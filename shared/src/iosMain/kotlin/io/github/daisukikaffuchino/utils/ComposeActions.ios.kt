package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard

@Composable
actual fun rememberCopyTextToClipboard(): (CharSequence) -> Unit = { text ->
    UIPasteboard.generalPasteboard.string = text.toString()
}

// TODO(iOS): UIActivityViewController 分享，需要拿到当前 UIViewController
@Composable
actual fun rememberShareText(): (CharSequence, CharSequence?) -> Unit = { _, _ -> }
