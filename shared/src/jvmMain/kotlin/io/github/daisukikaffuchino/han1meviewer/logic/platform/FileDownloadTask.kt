package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.DEFAULT_VIDEO_SUFFIX
import io.github.daisukikaffuchino.han1meviewer.HanimeDownloadLayout
import io.github.daisukikaffuchino.han1meviewer.logic.DatabaseRepo
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import io.github.daisukikaffuchino.han1meviewer.logic.network.ServiceCreator
import io.github.daisukikaffuchino.han1meviewer.logic.state.DownloadState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.write
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.github.daisukikaffuchino.han1meviewer.util.monotonicMillis
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.delay
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.write
import kotlin.time.Clock

/** 进度回写间隔，太密会把数据库刷爆。 */
private const val PROGRESS_FLUSH_INTERVAL_MILLIS = 800L
private const val BUFFER_SIZE = 64 * 1024

/**
 * 桌面的单个下载任务。取消协程即暂停，下次从已写入的长度继续。
 * 文件契约与 Android、iOS 一致（`<下载根目录>/hanime_download/<videoCode>/`），
 * 扫描导入三端通用。
 */
internal class FileDownloadTask(private val args: DownloadTaskArgs) {

    private val videoCode = args.videoCode
    private val quality = args.quality.orEmpty()
    private val suffix = args.videoType ?: DEFAULT_VIDEO_SUFFIX

    suspend fun run() {
        val url = args.downloadUrl ?: return markFailed()
        val folder = LocalDownloadStorage.videoFolder(videoCode)
        if (!folder.exists()) folder.createDirectories()

        val videoFile = folder / HanimeDownloadLayout.createVideoName(args.hanimeName, quality, suffix)
        val entity = ensureEntity(url, videoFile)

        downloadCoverIfNeeded(folder, entity)
        downloadVideo(url, videoFile, entity)
    }

    /** 库里没有就先建一条，长度用 Range 探一次；已存在则沿用（续传要看它的 downloadedLength）。 */
    private suspend fun ensureEntity(url: String, videoFile: PlatformFile): HanimeDownloadEntity {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let { return it }
        val entity = HanimeDownloadEntity(
            groupId = args.groupId,
            coverUrl = args.coverUrl,
            coverUri = null,
            title = args.hanimeName,
            addDate = Clock.System.now().toEpochMilliseconds(),
            videoCode = videoCode,
            videoUri = videoFile.path,
            quality = quality,
            videoUrl = url,
            length = probeContentLength(url),
            downloadedLength = 0L,
            state = DownloadState.Queued,
        )
        DatabaseRepo.HanimeDownload.insert(entity)
        return DatabaseRepo.HanimeDownload.find(videoCode, quality) ?: entity
    }

    /** 用 `Range: bytes=0-0` 换 Content-Range 里的总长度，跟 Android 那边同样的做法。 */
    private suspend fun probeContentLength(url: String): Long = runCatching {
        ServiceCreator.downloadClient.prepareGet(url) {
            header(HttpHeaders.Range, "bytes=0-0")
        }.execute { response ->
            response.headers[HttpHeaders.ContentRange]
                ?.substringAfter('/', "")
                ?.toLongOrNull()
                ?: response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                ?: 0L
        }
    }.getOrDefault(0L)

    private suspend fun downloadCoverIfNeeded(folder: PlatformFile, entity: HanimeDownloadEntity) {
        if (entity.coverUri != null || args.coverUrl.isBlank()) return
        val coverFile = folder / HanimeDownloadLayout.createVideoCoverName(
            args.hanimeName,
            HanimeDownloadLayout.DEF_VIDEO_COVER_TYPE,
        )
        runCatching {
            val bytes = ServiceCreator.downloadClient.prepareGet(args.coverUrl)
                .execute { it.readRawBytes() }
            coverFile.write(bytes)
            DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
                DatabaseRepo.HanimeDownload.update(it.copy(coverUri = coverFile.path))
            }
        }
    }

    private suspend fun downloadVideo(
        url: String,
        videoFile: PlatformFile,
        entity: HanimeDownloadEntity,
    ) {
        // 以文件实际大小为准，而不是库里的记录：上次可能是被强杀的
        var written = if (videoFile.exists()) videoFile.size() else 0L
        val needRange = written > 0L
        val path = Path(videoFile.path)

        ServiceCreator.downloadClient.prepareGet(url) {
            if (needRange) header(HttpHeaders.Range, "bytes=$written-")
        }.execute { response ->
            // 请求了续传却没给 206，说明服务端不支持，从头来
            val partial = response.status.value == 206
            if (needRange && !partial) {
                written = 0L
                SystemFileSystem.delete(path, mustExist = false)
            } else if (!needRange && !response.status.isSuccess()) {
                markFailed()
                return@execute
            }

            val total = entity.length.takeIf { it > 0L } ?: probeContentLength(url)
            markDownloading(written, total)

            val channel = response.bodyAsChannel()
            val buffer = ByteArray(BUFFER_SIZE)
            var lastFlush = Clock.System.now().toEpochMilliseconds()
            // 限速用本段已读字节与耗时算出该等多久。桌面上 OkHttp 的
            // SpeedLimitInterceptor 已经把流节流过了，这里算出来就是 0，不会重复限；
            // iOS 走 Darwin 引擎没有拦截器那一层，全靠这里。
            val throttleStart = monotonicMillis()
            var throttleRead = 0L

            SystemFileSystem.sink(path, append = written > 0L).buffered().use { sink ->
                while (true) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read <= 0) break
                    sink.write(buffer, 0, read)
                    written += read
                    throttleRead += read
                    val speedLimit = SettingsRepository.downloadSpeedLimit
                    if (speedLimit > 0L) {
                        val expected = throttleRead * 1000L / speedLimit
                        val elapsed = monotonicMillis() - throttleStart
                        if (expected > elapsed) delay(expected - elapsed)
                    }
                    val now = Clock.System.now().toEpochMilliseconds()
                    if (now - lastFlush >= PROGRESS_FLUSH_INTERVAL_MILLIS) {
                        lastFlush = now
                        sink.flush()
                        markDownloading(written, total)
                    }
                }
                sink.flush()
            }
            markFinished(written, total, videoFile.path)
        }
    }

    private suspend fun markDownloading(written: Long, total: Long) {
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

    private suspend fun markFinished(written: Long, total: Long, videoUri: String) {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
            DatabaseRepo.HanimeDownload.update(
                it.copy(
                    downloadedLength = written,
                    length = if (total > 0L) total else written,
                    videoUri = videoUri,
                    state = DownloadState.Finished,
                )
            )
        }
    }

    private suspend fun markFailed() {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
            DatabaseRepo.HanimeDownload.update(it.copy(state = DownloadState.Failed))
        }
    }

    /** 被取消时把状态落成暂停，不然重进页面会显示还在下载。 */
    suspend fun markPaused() {
        DatabaseRepo.HanimeDownload.find(videoCode, quality)?.let {
            if (it.state != DownloadState.Finished) {
                DatabaseRepo.HanimeDownload.update(it.copy(state = DownloadState.Paused))
            }
        }
    }

}
