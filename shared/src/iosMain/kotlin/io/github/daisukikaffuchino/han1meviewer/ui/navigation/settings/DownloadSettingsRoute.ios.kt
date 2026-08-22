package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.vinceglb.filekit.PlatformFile

// TODO(ios): 下载目录与迁移
actual fun getDownloadPath(): String? = null

actual suspend fun persistDownloadDirectory(file: PlatformFile) {
}

actual fun hasDownloadDirectoryPermission(): Boolean = false

actual fun setMaxConcurrentDownloadCount(value: Int) {
}

actual fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
) {
    onProgress(0, 0)
}
