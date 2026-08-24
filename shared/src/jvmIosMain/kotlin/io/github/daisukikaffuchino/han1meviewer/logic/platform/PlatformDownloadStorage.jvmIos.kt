package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual fun getDownloadPath(): String? = LocalDownloadStorage.displayPath()

actual suspend fun persistDownloadDirectory(file: PlatformFile): Boolean =
    LocalDownloadStorage.persist(file)

// 桌面和 iOS 都分「应用私有目录」和「用户选的外部目录」，换过目录之后旧文件要能搬过去
actual val isDownloadMigrationSupported: Boolean = true

actual fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
) {
    CoroutineScope(Dispatchers.Default).launch {
        migrateDownloadsToCurrentRoot(dao) { migrated, total ->
            // 回调那头要写 Compose 状态，跟 Android 侧一样切回主线程
            withContext(Dispatchers.Main) { onProgress(migrated, total) }
        }
    }
}

actual fun hasDownloadDirectoryPermission(): Boolean = LocalDownloadStorage.isUsable()

actual suspend fun deleteDownloadVideoFolder(videoCode: String) {
    LocalDownloadStorage.deleteVideoFolder(videoCode)
}

actual suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean =
    LocalDownloadStorage.scanAndImport(dao)

// 下载目录没选过就落在应用目录里，始终扫得到，不需要 Android 那套前提
actual fun canImportDownloadedVideos(): Boolean = LocalDownloadStorage.isUsable()
