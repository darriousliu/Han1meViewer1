package io.github.daisukikaffuchino.han1meviewer.logic.platform

import android.content.Intent
import androidx.documentfile.provider.DocumentFile
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.daisukikaffuchino.han1meviewer.util.SafFileManager
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.PlatformFile

actual fun getDownloadPath(): String? {
    val uri = SafFileManager.getSavedUri() ?: return null
    return if (SettingsRepository.isUsePrivateStorage) {
        applicationContext.getExternalFilesDir(null)?.absolutePath.orEmpty()
    } else {
        DocumentFile.fromTreeUri(applicationContext, uri)?.name ?: uri.toString()
    }
}

actual suspend fun persistDownloadDirectory(file: PlatformFile) {
    val uri = (file.androidFile as? AndroidFile.UriWrapper)?.uri ?: return
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    applicationContext.contentResolver.takePersistableUriPermission(uri, flags)
    SettingsRepository.setDownloadStorage(usePrivate = false, path = uri.toString())
}

actual fun hasDownloadDirectoryPermission(): Boolean =
    SafFileManager.checkSafPermissions(applicationContext)

actual suspend fun deleteDownloadVideoFolder(videoCode: String) {
    SafFileManager.deleteDownloadVideoFolder(applicationContext, videoCode)
}

actual suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean {
    if (!SafFileManager.checkSafPermissions(applicationContext)) return false
    SafFileManager.scanAndImportHanimeDownloads(applicationContext, dao)
    return true
}

actual val isDownloadMigrationSupported: Boolean = true

actual fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
) {
    SafFileManager.migratePrivateToSaf(applicationContext, dao, onProgress)
}
