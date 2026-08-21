package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun rememberCopyTextToClipboard(): (CharSequence) -> Unit = { text ->
    Toolkit.getDefaultToolkit().systemClipboard
        .setContents(StringSelection(text.toString()), null)
}

// TODO(JVM): 桌面端没有系统级分享，后续考虑复制链接或调 xdg-open
@Composable
actual fun rememberShareText(): (CharSequence, CharSequence?) -> Unit = { _, _ -> }
