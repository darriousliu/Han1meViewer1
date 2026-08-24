package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

@Composable
actual fun rememberReadClipboardText(): suspend () -> String? = {
    runCatching {
        Toolkit.getDefaultToolkit().systemClipboard
            .getData(DataFlavor.stringFlavor) as? String
    }.getOrNull()
}

@Composable
actual fun rememberCopyTextToClipboard(): (CharSequence) -> Unit = { text ->
    Toolkit.getDefaultToolkit().systemClipboard
        .setContents(StringSelection(text.toString()), null)
}
