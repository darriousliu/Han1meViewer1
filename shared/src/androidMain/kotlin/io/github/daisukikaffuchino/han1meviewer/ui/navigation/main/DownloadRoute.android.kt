package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import android.content.ClipData
import android.content.Intent
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_not_support
import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.daisukikaffuchino.han1meviewer.util.SafFileManager
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.getDownloadedHanimeVideoUri

actual fun openInExternalPlayer(
    videoUri: String,
    chooserTitle: String,
    onVideoMissing: () -> Unit,
) {
    val externalUri = applicationContext.getDownloadedHanimeVideoUri(videoUri, onVideoMissing)
        ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(externalUri, "video/*")
        clipData = ClipData.newRawUri("video", externalUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, chooserTitle).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { applicationContext.startActivity(chooser) }
        .onFailure { SonnerToast.warning(Res.string.action_not_support) }
}

actual suspend fun deleteDownloadVideoFolder(videoCode: String) {
    SafFileManager.deleteDownloadVideoFolder(applicationContext, videoCode)
}

actual suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean {
    if (!SafFileManager.checkSafPermissions(applicationContext)) return false
    SafFileManager.scanAndImportHanimeDownloads(applicationContext, dao)
    return true
}
