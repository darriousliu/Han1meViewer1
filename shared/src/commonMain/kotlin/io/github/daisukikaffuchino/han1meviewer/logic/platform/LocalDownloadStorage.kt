package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.HanimeDownloadLayout
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import io.github.daisukikaffuchino.han1meviewer.logic.state.DownloadState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

/**
 * 桌面与 iOS 的下载目录操作。这两端没有 SAF，下载目录就是一个普通路径，
 * 存进设置里即可；Android 走 SafFileManager，不经过这里。
 */
object LocalDownloadStorage {

    /** 文件名里的 `_1080P.mp4` 这一段，用来还原画质。 */
    private val qualityRegex = Regex("""_(\d+[Pp])\.[A-Za-z0-9]+$""")

    /**
     * 用户选过就用用户选的，没选过落到应用私有目录。
     *
     * 不要再拼 APP_NAME：FileKit.init(APP_NAME) 已经用应用名建好了 filesDir
     * （桌面是 ~/Library/Application Support/Han1meViewer），再拼一次会多出一层同名目录。
     */
    fun root(): PlatformFile =
        SettingsRepository.safDownloadPath?.takeIf { it.isNotBlank() }?.let(::PlatformFile)
            ?: FileKit.filesDir

    fun videoFolder(videoCode: String): PlatformFile =
        root() / HanimeDownloadLayout.HANIME_DOWNLOAD_FOLDER / videoCode

    fun displayPath(): String? = runCatching { root().path }.getOrNull()

    /** 目录不存在就先建出来，建不出来说明没权限。 */
    fun isUsable(): Boolean = runCatching {
        val dir = root()
        if (!dir.exists()) dir.createDirectories()
        dir.exists() && dir.isDirectory()
    }.getOrDefault(false)

    suspend fun persist(file: PlatformFile) {
        SettingsRepository.setDownloadStorage(usePrivate = false, path = file.path)
    }

    suspend fun deleteVideoFolder(videoCode: String) {
        runCatching { videoFolder(videoCode).deleteRecursively() }
    }

    /** 扫描下载目录，把里面已有的视频补进数据库。 */
    suspend fun scanAndImport(dao: HanimeDownloadDao): Boolean {
        val hanimeDir = root() / HanimeDownloadLayout.HANIME_DOWNLOAD_FOLDER
        if (!hanimeDir.exists() || !hanimeDir.isDirectory()) return false
        hanimeDir.list()
            .filter { it.isDirectory() }
            .forEach { folder -> runCatching { importOne(dao, folder) } }
        return true
    }

    private suspend fun importOne(dao: HanimeDownloadDao, folder: PlatformFile) {
        val videoCode = folder.name
        val children = folder.list()

        val videoFile = children.firstOrNull {
            it.suffix() in HanimeDownloadLayout.VIDEO_EXTENSIONS
        } ?: return
        val coverFile = children.firstOrNull {
            it.suffix() in HanimeDownloadLayout.IMAGE_EXTENSIONS
        }

        // info.json 优先取下载目录里的（跟 Android 产出的目录结构兼容），
        // 没有再回落到本端自己写的那份
        val infoFile = children.firstOrNull { it.name == HanimeDownloadLayout.VIDEO_INFO_FILE }
            ?: FileVideoCacheStore.infoFileOf(videoCode).takeIf { it.exists() }
        val info = infoFile?.let {
            runCatching { Json.parseToJsonElement(it.readBytes().decodeToString()).jsonObject }
                .getOrNull()
        }

        val quality = qualityRegex.find(videoFile.name)?.groupValues?.get(1) ?: "unknow"
        val videoUri = videoFile.path
        val coverUri = coverFile?.path

        val existing = dao.find(videoCode)
        if (existing != null) {
            dao.update(existing.copy(videoUri = videoUri, coverUri = coverUri))
            return
        }
        dao.insert(
            HanimeDownloadEntity(
                coverUrl = info?.get("coverUrl")?.jsonPrimitive?.contentOrNull.orEmpty(),
                coverUri = coverUri,
                title = info?.get("title")?.jsonPrimitive?.contentOrNull ?: videoCode,
                addDate = Clock.System.now().toEpochMilliseconds(),
                videoCode = videoCode,
                videoUri = videoUri,
                quality = quality,
                videoUrl = info?.get("videoUrls")?.jsonObject
                    ?.get(quality)?.jsonObject
                    ?.get("link")?.jsonPrimitive?.contentOrNull.orEmpty(),
                length = 0L,
                downloadedLength = 0L,
                state = DownloadState.Finished,
            )
        )
    }

    private fun PlatformFile.suffix(): String = name.substringAfterLast('.', "").lowercase()

    private suspend fun PlatformFile.deleteRecursively() {
        if (!exists()) return
        if (isDirectory()) list().forEach { it.deleteRecursively() }
        delete()
    }
}
