package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_not_support
import io.github.daisukikaffuchino.utils.SonnerToast
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

// iOS 不允许应用自行重启/退出，这两个操作留空
actual fun restartApplication() = Unit

actual val canRestartApplication: Boolean = false

@Composable
actual fun rememberExitApp(): () -> Unit = {}

// iOS 没有防截屏，也没有「重建 Activity」的概念，返回 null 让调用方隐藏/跳过
@Composable
actual fun rememberSetSecureMode(): ((Boolean) -> Unit)? = null

@Composable
actual fun rememberRecreateScreen(): (() -> Unit)? = null

// iOS 没有对应的系统设置入口，返回 null 让设置项直接不显示
@Composable
actual fun rememberOpenDeepLinkSettings(): (() -> Unit)? = null

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
