package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.DatabaseRepo
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import io.github.daisukikaffuchino.han1meviewer.logic.state.DownloadState
import io.github.daisukikaffuchino.utils.LogUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 桌面的下载队列。
 *
 * Android 用 WorkManager，iOS 用 NSURLSession 的后台会话，桌面两者都没有，
 * 这里是纯协程实现——桌面本来也没有「应用被系统挂起」这回事。
 */
internal object FileDownloadWorkController : DownloadWorkController {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()

    /** key 是 videoCode + quality，跟数据库的唯一键对齐。 */
    private val activeJobs = mutableMapOf<String, Job>()
    private val waiting = ArrayDeque<DownloadTaskArgs>()
    private val runningCount = MutableStateFlow(0)

    private val maxConcurrent: Int
        get() = SettingsRepository.downloadCountLimit.coerceAtLeast(1)

    private fun keyOf(videoCode: String, quality: String?) = "$videoCode#${quality.orEmpty()}"

    private fun HanimeDownloadEntity.toArgs() = DownloadTaskArgs(
        quality = quality,
        downloadUrl = videoUrl,
        videoType = suffix,
        hanimeName = title,
        videoCode = videoCode,
        coverUrl = coverUrl,
        groupId = groupId,
    )

    // 没有 WorkManager 的历史记录要清
    override fun prune() = Unit

    /** 上次是被强杀的，库里可能留着「下载中」，先落成暂停等用户手动继续。 */
    override suspend fun initialize() {
        runCatching { DatabaseRepo.HanimeDownload.pauseAll() }
            .onFailure { LogUtil.e("Download", "初始化下载队列失败", it) }
    }

    override fun runningCount(): Flow<Int> = runningCount.map { it }

    override fun pause(entity: HanimeDownloadEntity) {
        scope.launch {
            val key = keyOf(entity.videoCode, entity.quality)
            mutex.withLock {
                waiting.removeAll { keyOf(it.videoCode, it.quality) == key }
                activeJobs.remove(key)
            }?.cancel()
            FileDownloadTask(entity.toArgs()).markPaused()
            pumpQueue()
        }
    }

    override fun resume(entity: HanimeDownloadEntity) = enqueue(entity.toArgs())

    override fun delete(entity: HanimeDownloadEntity) {
        scope.launch {
            val key = keyOf(entity.videoCode, entity.quality)
            mutex.withLock {
                waiting.removeAll { keyOf(it.videoCode, it.quality) == key }
                activeJobs.remove(key)
            }?.cancel()
            runCatching {
                LocalDownloadStorage.deleteVideoFolder(entity.videoCode)
                DatabaseRepo.HanimeDownload.delete(entity)
            }.onFailure { LogUtil.e("Download", "删除下载失败", it) }
            pumpQueue()
        }
    }

    override fun enqueue(args: DownloadTaskArgs, redownload: Boolean) {
        scope.launch {
            val key = keyOf(args.videoCode, args.quality)
            if (redownload) {
                runCatching {
                    LocalDownloadStorage.deleteVideoFolder(args.videoCode)
                    DatabaseRepo.HanimeDownload.delete(args.videoCode, args.quality.orEmpty())
                }
            }
            mutex.withLock {
                if (activeJobs.containsKey(key)) return@launch
                if (waiting.any { keyOf(it.videoCode, it.quality) == key }) return@launch
                waiting.addLast(args)
            }
            markQueued(args)
            pumpQueue()
        }
    }

    /** 有空位就从等待队列里补人。 */
    private suspend fun pumpQueue() {
        while (true) {
            val next = mutex.withLock {
                if (activeJobs.size >= maxConcurrent) return
                waiting.removeFirstOrNull()?.also { args ->
                    // 先占位再启动，避免并发调用超发
                    activeJobs[keyOf(args.videoCode, args.quality)] = Job()
                }
            } ?: return
            start(next)
        }
    }

    private suspend fun start(args: DownloadTaskArgs) {
        val key = keyOf(args.videoCode, args.quality)
        val task = FileDownloadTask(args)
        val job = scope.launch {
            runCatching { task.run() }
                .onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    LogUtil.e("Download", "下载失败: ${args.videoCode}", throwable)
                }
        }
        mutex.withLock {
            activeJobs[key]?.cancel()
            activeJobs[key] = job
            runningCount.value = activeJobs.size
        }
        job.invokeOnCompletion {
            scope.launch {
                mutex.withLock {
                    if (activeJobs[key] === job) activeJobs.remove(key)
                    runningCount.value = activeJobs.size
                }
                pumpQueue()
            }
        }
    }

    private suspend fun markQueued(args: DownloadTaskArgs) {
        runCatching {
            DatabaseRepo.HanimeDownload.find(args.videoCode, args.quality.orEmpty())?.let {
                if (it.state != DownloadState.Finished) {
                    DatabaseRepo.HanimeDownload.update(it.copy(state = DownloadState.Queued))
                }
            }
        }
    }
}
