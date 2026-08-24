package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.HanimeDownloadLayout
import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
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

/**
 * 把一个文件挪到新位置。
 *
 * 不走读字节再写：视频动辄上 G，整个读进内存不现实；同卷上系统层面就是改个名。
 */
internal expect suspend fun movePlatformFile(from: PlatformFile, to: PlatformFile): Boolean

/**
 * 把应用私有目录里的下载搬到用户当前选的下载目录，搬完扫一遍把库里的 uri 指过去。
 *
 * onProgress 的约定跟 Android 那侧一致：total 为 0 表示没有可迁移的文件，-1 表示没权限。
 */
internal suspend fun migrateDownloadsToCurrentRoot(
    dao: HanimeDownloadDao?,
    onProgress: suspend (migrated: Int, total: Int) -> Unit,
) {
    LocalDownloadStorage.refreshBookmarkIfNeeded()
    val target = LocalDownloadStorage.root()
    val source = FileKit.filesDir
    // 没选过外部目录时两边是同一个地方，没有可迁移的东西
    if (source.path == target.path) return onProgress(0, 0)

    val sourceRoot = source / HanimeDownloadLayout.HANIME_DOWNLOAD_FOLDER
    if (!sourceRoot.exists() || !sourceRoot.isDirectory()) return onProgress(0, 0)

    val folders = sourceRoot.list().filter { it.isDirectory() }
    val files = folders.flatMap { folder -> folder.list().filter { !it.isDirectory() } }
    if (files.isEmpty()) return onProgress(0, 0)
    if (!LocalDownloadStorage.isUsable()) return onProgress(0, -1)

    val targetRoot = target / HanimeDownloadLayout.HANIME_DOWNLOAD_FOLDER
    runCatching { targetRoot.createDirectories() }

    var migrated = 0
    folders.forEach { folder ->
        val destFolder = targetRoot / folder.name
        runCatching { destFolder.createDirectories() }
        folder.list().filter { !it.isDirectory() }.forEach { file ->
            movePlatformFile(file, destFolder / file.name)
            migrated++
            onProgress(migrated, files.size)
        }
        // 搬空了才删，还剩东西说明有文件没搬成功
        runCatching { if (folder.list().isEmpty()) folder.delete() }
    }
    runCatching { if (sourceRoot.list().isEmpty()) sourceRoot.delete() }
    // 库里的 videoUri/coverUri 还指着旧路径，扫一遍指过去
    dao?.let { runCatching { LocalDownloadStorage.scanAndImport(it) } }
}
