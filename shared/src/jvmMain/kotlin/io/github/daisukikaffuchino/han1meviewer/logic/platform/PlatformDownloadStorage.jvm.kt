package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.vinceglb.filekit.PlatformFile

actual fun getDownloadPath(): String? = LocalDownloadStorage.displayPath()

actual suspend fun persistDownloadDirectory(file: PlatformFile) {
    LocalDownloadStorage.persist(file)
}

actual fun hasDownloadDirectoryPermission(): Boolean = LocalDownloadStorage.isUsable()

actual suspend fun deleteDownloadVideoFolder(videoCode: String) {
    LocalDownloadStorage.deleteVideoFolder(videoCode)
}

actual suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean =
    LocalDownloadStorage.scanAndImport(dao)

// 桌面端没有「应用私有目录 vs 公共目录」之分
actual val isDownloadMigrationSupported: Boolean = false

// 桌面端没有「应用私有目录 vs 公共目录」之分，没有可迁移的东西
actual fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
) {
    onProgress(0, 0)
}
