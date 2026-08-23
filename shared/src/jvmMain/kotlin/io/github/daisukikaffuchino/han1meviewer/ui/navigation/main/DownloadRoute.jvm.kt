package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_not_support
import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.daisukikaffuchino.han1meviewer.logic.platform.LocalDownloadStorage
import io.github.daisukikaffuchino.utils.SonnerToast
import java.awt.Desktop
import java.io.File

/** 桌面端交给系统关联的播放器打开，没有 Android 那种选择器，chooserTitle 用不上。 */
actual fun openInExternalPlayer(
    videoUri: String,
    chooserTitle: String,
    onVideoMissing: () -> Unit,
) {
    val file = File(videoUri.removePrefix("file://"))
    if (!file.isFile) {
        onVideoMissing()
        return
    }
    val desktop = runCatching {
        Desktop.getDesktop().takeIf { it.isSupported(Desktop.Action.OPEN) }
    }.getOrNull()
    if (desktop == null) {
        SonnerToast.warning(Res.string.action_not_support)
        return
    }
    runCatching { desktop.open(file) }
        .onFailure { SonnerToast.warning(Res.string.action_not_support) }
}

actual suspend fun deleteDownloadVideoFolder(videoCode: String) {
    LocalDownloadStorage.deleteVideoFolder(videoCode)
}

actual suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean =
    LocalDownloadStorage.scanAndImport(dao)
