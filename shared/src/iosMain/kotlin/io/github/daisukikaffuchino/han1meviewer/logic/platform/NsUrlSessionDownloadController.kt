package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.DEFAULT_VIDEO_SUFFIX
import io.github.daisukikaffuchino.han1meviewer.HanimeDownloadLayout
import io.github.daisukikaffuchino.han1meviewer.logic.DatabaseRepo
import io.github.daisukikaffuchino.han1meviewer.logic.network.ServiceCreator
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import io.github.daisukikaffuchino.han1meviewer.logic.state.DownloadState
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.readRawBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDownloadDelegateProtocol
import platform.Foundation.NSURLSessionDownloadTask
import platform.Foundation.NSURLSessionTask
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.darwin.NSObject

private const val SESSION_ID = "io.github.daisukikaffuchino.han1meviewer.download"
private const val RESUME_DATA_FILE = ".resume"

/**
 * iOS 的下载队列，走 NSURLSession 的后台会话。
 *
 * 桌面那套是自己读流写文件的协程实现，iOS 用不了——那样切到后台就被系统挂起。
 * 后台会话由系统负责传输：进程被杀也会继续，下完再把 app 唤回来。代价有两个，
 * 都是这条路径固有的：
 *  - 拿不到字节流，所以**限速在 iOS 上不可能**（设置项已按平台隐藏）；
 *  - 并发由系统调度，downloadCountLimit 只能当建议。
 */
@OptIn(ExperimentalForeignApi::class)
internal object NsUrlSessionDownloadController : DownloadWorkController {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val runningTasks = MutableStateFlow(0)

    /** taskDescription 里存的 key，跟数据库唯一键对齐。 */
    private fun keyOf(videoCode: String, quality: String?) = "$videoCode#${quality.orEmpty()}"
    private fun splitKey(key: String) = key.substringBefore('#') to key.substringAfter('#')

    private val delegate = object : NSObject(), NSURLSessionDownloadDelegateProtocol {

        /**
         * 文件下完在临时位置，**必须在这个回调里同步搬走**，回调一返回系统就删了。
         */
        override fun URLSession(
            session: NSURLSession,
            downloadTask: NSURLSessionDownloadTask,
            didFinishDownloadingToURL: NSURL,
        ) {
            val key = downloadTask.taskDescription ?: return
            val (videoCode, quality) = splitKey(key)
            val target = runBlocking { resolveTargetFile(videoCode, quality) } ?: return
            val moved = memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                NSFileManager.defaultManager.moveItemAtURL(
                    didFinishDownloadingToURL,
                    NSURL.fileURLWithPath(target.path),
                    error.ptr,
                ).also { if (!it) LogUtil.e("Download", "搬运失败: ${error.value?.localizedDescription}") }
            }
            if (!moved) return
            scope.launch { markFinished(videoCode, quality, target.path) }
        }

        override fun URLSession(
            session: NSURLSession,
            downloadTask: NSURLSessionDownloadTask,
            didWriteData: Long,
            totalBytesWritten: Long,
            totalBytesExpectedToWrite: Long,
        ) {
            val key = downloadTask.taskDescription ?: return
            val (videoCode, quality) = splitKey(key)
            scope.launch {
                markDownloading(videoCode, quality, totalBytesWritten, totalBytesExpectedToWrite)
            }
        }

        override fun URLSession(
            session: NSURLSession,
            task: NSURLSessionTask,
            didCompleteWithError: NSError?,
        ) {
            refreshRunningCount()
            val error = didCompleteWithError ?: return
            val key = task.taskDescription ?: return
            val (videoCode, quality) = splitKey(key)
            // 暂停也是以「错误」形式回来的，此时会带上续传数据，别误判成失败
            val resumeData = error.userInfo["NSURLSessionDownloadTaskResumeData"] as? NSData
            scope.launch {
                if (resumeData != null) {
                    saveResumeData(videoCode, resumeData)
                    markPaused(videoCode, quality)
                } else {
                    LogUtil.e("Download", "下载失败 $key: ${error.localizedDescription}")
                    markFailed(videoCode, quality)
                }
            }
        }

        /** 后台事件处理完，必须回调系统给的 handler。 */
        override fun URLSessionDidFinishEventsForBackgroundURLSession(session: NSURLSession) {
            BackgroundDownloadEvents.onBackgroundSessionFinished?.invoke()
            BackgroundDownloadEvents.onBackgroundSessionFinished = null
        }
    }

    private val session: NSURLSession by lazy {
        NSURLSession.sessionWithConfiguration(
            configuration = NSURLSessionConfiguration
                .backgroundSessionConfigurationWithIdentifier(SESSION_ID),
            delegate = delegate,
            delegateQueue = NSOperationQueue.mainQueue,
        )
    }

    // 后台会话本身就是持久的，没有历史记录要清
    override fun prune() = Unit

    /**
     * 重新接管上次留下的任务。后台会话用同一个 identifier 建起来时，系统会把还在跑的
     * 任务交回来；库里那些没有对应任务的「下载中」落成暂停。
     */
    override suspend fun initialize() {
        session.getTasksWithCompletionHandler { _, _, downloads ->
            val alive = downloads.orEmpty()
                .filterIsInstance<NSURLSessionDownloadTask>()
                .mapNotNull { it.taskDescription }
                .toSet()
            runningTasks.value = alive.size
            scope.launch { pauseOrphans(alive) }
        }
    }

    override fun runningCount(): Flow<Int> = runningTasks.asStateFlow()

    override fun pause(entity: HanimeDownloadEntity) {
        val key = keyOf(entity.videoCode, entity.quality)
        session.getTasksWithCompletionHandler { _, _, downloads ->
            downloads.orEmpty()
                .filterIsInstance<NSURLSessionDownloadTask>()
                .firstOrNull { it.taskDescription == key }
                // 续传数据在 didCompleteWithError 里落盘，这里不用管
                ?.cancelByProducingResumeData { }
            refreshRunningCount()
        }
    }

    override fun resume(entity: HanimeDownloadEntity) = enqueue(entity.toArgs())

    override fun delete(entity: HanimeDownloadEntity) {
        val key = keyOf(entity.videoCode, entity.quality)
        session.getTasksWithCompletionHandler { _, _, downloads ->
            downloads.orEmpty()
                .filterIsInstance<NSURLSessionDownloadTask>()
                .firstOrNull { it.taskDescription == key }
                ?.cancel()
            refreshRunningCount()
        }
        scope.launch {
            runCatching {
                LocalDownloadStorage.deleteVideoFolder(entity.videoCode)
                DatabaseRepo.HanimeDownload.delete(entity)
            }.onFailure { LogUtil.e("Download", "删除下载失败", it) }
        }
    }

    override fun enqueue(args: DownloadTaskArgs, redownload: Boolean) {
        val url = args.downloadUrl ?: return
        val key = keyOf(args.videoCode, args.quality)
        scope.launch {
            if (redownload) {
                runCatching {
                    LocalDownloadStorage.deleteVideoFolder(args.videoCode)
                    DatabaseRepo.HanimeDownload.delete(args.videoCode, args.quality.orEmpty())
                }
            }
            ensureEntity(args, url)
            downloadCoverIfNeeded(args)

            // 有续传数据就接着下，没有才从头开始
            val resume = readResumeData(args.videoCode)
            val task = if (resume != null) {
                session.downloadTaskWithResumeData(resume)
            } else {
                val nsUrl = NSURL.URLWithString(url) ?: return@launch
                session.downloadTaskWithURL(nsUrl)
            }
            task.taskDescription = key
            task.resume()
            clearResumeData(args.videoCode)
            markQueued(args.videoCode, args.quality)
            refreshRunningCount()
        }
    }

    private fun refreshRunningCount() {
        session.getTasksWithCompletionHandler { _, _, downloads ->
            runningTasks.value = downloads.orEmpty().size
        }
    }

    // ---- 数据库 ----

    private fun HanimeDownloadEntity.toArgs() = DownloadTaskArgs(
        quality = quality,
        downloadUrl = videoUrl,
        videoType = suffix,
        hanimeName = title,
        videoCode = videoCode,
        coverUrl = coverUrl,
        groupId = groupId,
    )

    private suspend fun ensureEntity(args: DownloadTaskArgs, url: String) {
        val quality = args.quality.orEmpty()
        if (DatabaseRepo.HanimeDownload.find(args.videoCode, quality) != null) return
        val target = targetFile(args.videoCode, args.hanimeName, quality, args.videoType)
        DatabaseRepo.HanimeDownload.insert(
            HanimeDownloadEntity(
                groupId = args.groupId,
                coverUrl = args.coverUrl,
                coverUri = null,
                title = args.hanimeName,
                addDate = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                videoCode = args.videoCode,
                videoUri = target.path,
                quality = quality,
                videoUrl = url,
                // 总长度等系统在 didWriteData 里报回来
                length = 0L,
                downloadedLength = 0L,
                state = DownloadState.Queued,
            )
        )
    }

    /** 封面很小，没必要占后台会话的名额，直接用 Ktor 前台拉。 */
    private suspend fun downloadCoverIfNeeded(args: DownloadTaskArgs) {
        val quality = args.quality.orEmpty()
        val entity = DatabaseRepo.HanimeDownload.find(args.videoCode, quality) ?: return
        if (entity.coverUri != null || args.coverUrl.isBlank()) return
        runCatching {
            val folder = LocalDownloadStorage.videoFolder(args.videoCode)
            if (!folder.exists()) folder.createDirectories()
            val cover = folder / HanimeDownloadLayout.createVideoCoverName(
                args.hanimeName,
                HanimeDownloadLayout.DEF_VIDEO_COVER_TYPE,
            )
            cover.write(ServiceCreator.downloadClient.prepareGet(args.coverUrl).execute { it.readRawBytes() })
            DatabaseRepo.HanimeDownload.find(args.videoCode, quality)?.let {
                DatabaseRepo.HanimeDownload.update(it.copy(coverUri = cover.path))
            }
        }
    }

    private fun targetFile(
        videoCode: String,
        title: String,
        quality: String,
        videoType: String?,
    ): PlatformFile {
        val folder = LocalDownloadStorage.videoFolder(videoCode)
        if (!folder.exists()) folder.createDirectories()
        return folder / HanimeDownloadLayout.createVideoName(
            title,
            quality,
            videoType ?: DEFAULT_VIDEO_SUFFIX,
        )
    }

    private suspend fun resolveTargetFile(videoCode: String, quality: String): PlatformFile? {
        val entity = DatabaseRepo.HanimeDownload.find(videoCode, quality) ?: return null
        return targetFile(videoCode, entity.title, quality, entity.suffix)
    }

    private suspend fun markQueued(videoCode: String, quality: String?) {
        DatabaseRepo.HanimeDownload.find(videoCode, quality.orEmpty())?.let {
            if (it.state != DownloadState.Finished) {
                DatabaseRepo.HanimeDownload.update(it.copy(state = DownloadState.Queued))
            }
        }
    }

    private suspend fun markDownloading(
        videoCode: String,
        quality: String,
        written: Long,
        total: Long,
    ) {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
            DatabaseRepo.HanimeDownload.update(
                it.copy(
                    downloadedLength = written,
                    length = if (total > 0L) total else it.length,
                    state = DownloadState.Downloading,
                )
            )
        }
    }

    private suspend fun markFinished(videoCode: String, quality: String, videoUri: String) {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
            DatabaseRepo.HanimeDownload.update(
                it.copy(
                    downloadedLength = it.length.takeIf { len -> len > 0L } ?: it.downloadedLength,
                    videoUri = videoUri,
                    state = DownloadState.Finished,
                )
            )
        }
    }

    private suspend fun markPaused(videoCode: String, quality: String) {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
            if (it.state != DownloadState.Finished) {
                DatabaseRepo.HanimeDownload.update(it.copy(state = DownloadState.Paused))
            }
        }
    }

    private suspend fun markFailed(videoCode: String, quality: String) {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
            DatabaseRepo.HanimeDownload.update(it.copy(state = DownloadState.Failed))
        }
    }

    private suspend fun pauseOrphans(aliveKeys: Set<String>) {
        runCatching {
            DatabaseRepo.HanimeDownload.pauseAll()
            aliveKeys.forEach { key ->
                val (videoCode, quality) = splitKey(key)
                DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
                    DatabaseRepo.HanimeDownload.update(it.copy(state = DownloadState.Downloading))
                }
            }
        }.onFailure { LogUtil.e("Download", "接管后台任务失败", it) }
    }

    // ---- 续传数据 ----

    private fun resumeFile(videoCode: String) =
        LocalDownloadStorage.videoFolder(videoCode) / RESUME_DATA_FILE

    private suspend fun saveResumeData(videoCode: String, data: NSData) {
        runCatching {
            val folder = LocalDownloadStorage.videoFolder(videoCode)
            if (!folder.exists()) folder.createDirectories()
            data.writeToFile(resumeFile(videoCode).path, atomically = true)
        }
    }

    private suspend fun readResumeData(videoCode: String): NSData? = runCatching {
        val file = resumeFile(videoCode)
        if (!file.exists()) return null
        NSData.dataWithContentsOfURL(NSURL.fileURLWithPath(file.path))
    }.getOrNull()

    private suspend fun clearResumeData(videoCode: String) {
        runCatching {
            val file = resumeFile(videoCode)
            if (file.exists()) file.delete()
        }
    }
}
