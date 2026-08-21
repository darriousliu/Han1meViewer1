package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

@Composable
actual fun rememberReadClipboardText(): suspend () -> String? = {
    runCatching {
        Toolkit.getDefaultToolkit().systemClipboard
            .getData(DataFlavor.stringFlavor) as? String
    }.getOrNull()
}
