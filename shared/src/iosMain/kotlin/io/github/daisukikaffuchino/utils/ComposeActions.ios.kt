package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.han1meviewer.util.topMostViewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIPasteboard
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberCopyTextToClipboard(): (CharSequence) -> Unit = { text ->
    UIPasteboard.generalPasteboard.string = text.toString()
}

/** 系统分享面板；title 由面板自己决定，用不上。 */
@Composable
actual fun rememberShareText(): (CharSequence, CharSequence?) -> Unit = { content, _ ->
    val host = topMostViewController()
    if (host != null) {
        val controller = UIActivityViewController(
            activityItems = listOf(content.toString()),
            applicationActivities = null,
        )
        // iPad 上 popover 没有锚点会直接崩
        controller.popoverPresentationController?.sourceView = host.view
        host.presentViewController(controller, animated = true, completion = null)
    }
}
