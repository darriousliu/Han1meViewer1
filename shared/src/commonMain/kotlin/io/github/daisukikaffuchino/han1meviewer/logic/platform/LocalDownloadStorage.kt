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
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.resolveBookmarkData
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import kotlin.io.encoding.Base64
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

    /** 解析好的外部目录。安全作用域只开一次，别每次 root() 都开。 */
    private var cachedExternalRoot: Pair<String, PlatformFile>? = null

    /** bookmark 过期了（目录被改名/移动过），解析还能成功但该重建一份存回去。 */
    private var bookmarkNeedsRefresh = false

    /**
     * 用户选过就用用户选的，没选过落到应用私有目录。
     *
     * 不要再拼 APP_NAME：FileKit.init(APP_NAME) 已经用应用名建好了 filesDir
     * （桌面是 ~/Library/Application Support/Han1meViewer），再拼一次会多出一层同名目录。
     *
     * 外部目录必须从 bookmark 解析：iOS 上文档选择器给的目录在沙盒外，拿路径重新
     * 构造出来的 URL 没有安全作用域，读写一律失败。
     */
    fun root(): PlatformFile = externalRoot() ?: FileKit.filesDir

    private fun externalRoot(): PlatformFile? {
        val bookmark = SettingsRepository.downloadDirBookmark?.takeIf { it.isNotBlank() }
            ?: return null
        cachedExternalRoot?.let { (key, file) -> if (key == bookmark) return file }
        val resolution = runCatching {
            PlatformFile.resolveBookmarkData(Base64.decode(bookmark))
        }.getOrNull() ?: return null
        bookmarkNeedsRefresh = resolution.shouldRefresh
        val file = resolution.file
        file.startAccessingSecurityScopedResource()
        cachedExternalRoot = bookmark to file
        return file
    }

    /**
     * bookmark 过期时重建一份存回去。
     *
     * 过期的 bookmark 仍然解析得出目录，所以这是修复而不是前置条件；
     * 不修的话哪天彻底失效就会静默退回应用私有目录。
     */
    suspend fun refreshBookmarkIfNeeded() {
        if (!bookmarkNeedsRefresh) return
        bookmarkNeedsRefresh = false
        val current = externalRoot() ?: return
        runCatching { Base64.encode(current.bookmarkData().bytes) }.getOrNull()?.let { fresh ->
            cachedExternalRoot = null
            SettingsRepository.setDownloadStorage(
                usePrivate = false,
                path = current.path,
                bookmark = fresh,
            )
        }
    }

    fun videoFolder(videoCode: String): PlatformFile =
        root() / HanimeDownloadLayout.HANIME_DOWNLOAD_FOLDER / videoCode

    fun displayPath(): String? = runCatching { root().path }.getOrNull()

    /** 目录不存在就先建出来，建不出来说明没权限。 */
    fun isUsable(): Boolean = runCatching {
        val dir = root()
        if (!dir.exists()) dir.createDirectories()
        dir.exists() && dir.isDirectory()
    }.getOrDefault(false)

    /** bookmark 存不下来就别改设置：那样只会得到一个选了却读不了的目录。 */
    suspend fun persist(file: PlatformFile): Boolean {
        val bookmark = runCatching { Base64.encode(file.bookmarkData().bytes) }.getOrNull()
            ?: return false
        cachedExternalRoot = null
        SettingsRepository.setDownloadStorage(
            usePrivate = false,
            path = file.path,
            bookmark = bookmark,
        )
        return true
    }

    suspend fun deleteVideoFolder(videoCode: String) {
        runCatching { videoFolder(videoCode).deleteRecursively() }
    }

    /** 扫描下载目录，把里面已有的视频补进数据库。 */
    suspend fun scanAndImport(dao: HanimeDownloadDao): Boolean {
        refreshBookmarkIfNeeded()
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
