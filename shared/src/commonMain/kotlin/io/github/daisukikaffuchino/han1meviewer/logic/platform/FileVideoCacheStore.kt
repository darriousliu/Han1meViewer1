package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.DEFAULT_VIDEO_SUFFIX
import io.github.daisukikaffuchino.han1meviewer.HJson
import io.github.daisukikaffuchino.han1meviewer.HanimeLink
import io.github.daisukikaffuchino.han1meviewer.logic.DatabaseRepo
import io.github.daisukikaffuchino.han1meviewer.logic.model.HanimeVideo
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * 桌面与 iOS 共用的视频信息缓存。
 *
 * Android 把 info.json 放在下载目录里（走 SAF），这两端没有 SAF，改放应用私有目录：
 * 与用户可改的下载目录解耦，换目录也不会丢标题、封面这些信息。
 */
object FileVideoCacheStore : VideoCacheStore {

    private const val DIR_NAME = "video_info"

    private val dir: PlatformFile get() = FileKit.filesDir / DIR_NAME

    /** 扫描导入时若下载目录里没有 info.json，会回落到这里。 */
    internal fun infoFileOf(videoCode: String): PlatformFile = dir / "$videoCode.json"

    override fun load(videoCode: String): Flow<HanimeVideo?> = flow {
        val entity = DatabaseRepo.HanimeDownload.find(videoCode)
        if (entity == null) {
            emit(null)
            return@flow
        }
        val file = infoFileOf(videoCode)
        val info = runCatching {
            if (file.exists()) {
                HJson.decodeFromString<HanimeVideo>(file.readBytes().decodeToString())
            } else null
        }.getOrNull()
        // 播放地址与封面一律以本地下载的为准
        emit(
            info?.copy(
                videoUrls = linkedMapOf(
                    entity.quality to HanimeLink(entity.videoUri, DEFAULT_VIDEO_SUFFIX)
                ),
                coverUrl = entity.coverUri ?: entity.coverUrl,
            )
        )
    }.flowOn(Dispatchers.IO)

    override suspend fun save(videoCode: String, info: HanimeVideo) = withContext(Dispatchers.IO) {
        val dir = dir
        if (!dir.exists()) dir.createDirectories()
        (dir / "$videoCode.json").write(HJson.encodeToString(info).encodeToByteArray())
    }
}
