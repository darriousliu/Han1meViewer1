package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.copy_to_clipboard

/** 桌面端没有系统分享面板，退而求其次把内容复制到剪贴板。 */
@Composable
actual fun rememberShareText(): (CharSequence, CharSequence?) -> Unit {
    val copy = rememberCopyTextToClipboard()
    return { content, _ ->
        copy(content)
        SonnerToast.success(Res.string.copy_to_clipboard)
    }
}
