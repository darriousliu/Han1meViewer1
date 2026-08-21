package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard

@Composable
actual fun rememberReadClipboardText(): suspend () -> String? = {
    UIPasteboard.generalPasteboard.string
}
