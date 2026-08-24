package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.daisukikaffuchino.han1meviewer.logic.platform.LocalDownloadStorage
import io.github.vinceglb.filekit.PlatformFile

actual fun getDownloadPath(): String? = LocalDownloadStorage.displayPath()

actual suspend fun persistDownloadDirectory(file: PlatformFile) {
    LocalDownloadStorage.persist(file)
}

actual fun hasDownloadDirectoryPermission(): Boolean = LocalDownloadStorage.isUsable()

// 队列每次补人时现读 SettingsRepository.downloadCountLimit，
// 设置已经写进去了，这里不用再推一次
actual fun setMaxConcurrentDownloadCount(value: Int) = Unit

// 桌面端没有「应用私有目录 vs 公共目录」之分，没有可迁移的东西
actual fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
) {
    onProgress(0, 0)
}

// 桌面端没有「应用私有目录 vs 公共目录」之分
actual val isDownloadMigrationSupported: Boolean = false

// 走自己读流的下载实现，能限速
actual val isDownloadSpeedLimitSupported: Boolean = true
