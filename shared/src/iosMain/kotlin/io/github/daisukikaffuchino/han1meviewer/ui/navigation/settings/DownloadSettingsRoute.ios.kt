package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.daisukikaffuchino.han1meviewer.logic.platform.LocalDownloadStorage
import io.github.vinceglb.filekit.PlatformFile

actual fun getDownloadPath(): String? = LocalDownloadStorage.displayPath()

actual suspend fun persistDownloadDirectory(file: PlatformFile) {
    LocalDownloadStorage.persist(file)
}

actual fun hasDownloadDirectoryPermission(): Boolean = LocalDownloadStorage.isUsable()

// TODO(ios): 依赖后台下载实现，等下载内核补上再接
actual fun setMaxConcurrentDownloadCount(value: Int) {
}

// iOS没有「应用私有目录 vs 公共目录」之分，没有可迁移的东西
actual fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
) {
    onProgress(0, 0)
}

// iOS 的下载目录始终在应用沙盒内，没有可迁移的去处
actual val isDownloadMigrationSupported: Boolean = false
