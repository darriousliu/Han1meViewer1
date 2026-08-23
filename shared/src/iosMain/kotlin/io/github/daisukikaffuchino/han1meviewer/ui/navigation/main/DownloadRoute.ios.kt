package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_not_support
import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.daisukikaffuchino.han1meviewer.logic.platform.LocalDownloadStorage
import io.github.daisukikaffuchino.han1meviewer.util.topMostViewController
import io.github.daisukikaffuchino.utils.SonnerToast
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

/**
 * iOS 没有「用其他应用打开本地文件」的直接入口，用系统分享面板代替，
 * 里面就有各家播放器的「拷贝到…」。chooserTitle 由系统面板自己决定，用不上。
 */
actual fun openInExternalPlayer(
    videoUri: String,
    chooserTitle: String,
    onVideoMissing: () -> Unit,
) {
    val path = videoUri.removePrefix("file://")
    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
        onVideoMissing()
        return
    }
    val host = topMostViewController()
    if (host == null) {
        SonnerToast.warning(Res.string.action_not_support)
        return
    }
    val controller = UIActivityViewController(
        activityItems = listOf(NSURL.fileURLWithPath(path)),
        applicationActivities = null,
    )
    // iPad 上 popover 没有锚点会直接崩
    controller.popoverPresentationController?.sourceView = host.view
    host.presentViewController(controller, animated = true, completion = null)
}

actual suspend fun deleteDownloadVideoFolder(videoCode: String) {
    LocalDownloadStorage.deleteVideoFolder(videoCode)
}

actual suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean =
    LocalDownloadStorage.scanAndImport(dao)
