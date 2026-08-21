package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberReadClipboardText(): suspend () -> String? {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    return {
        clipboard.getClipEntry()
            ?.clipData
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
    }
}
