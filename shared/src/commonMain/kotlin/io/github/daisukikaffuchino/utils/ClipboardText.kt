package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable

/** 读系统剪贴板里的纯文本，读不到返回 null。 */
@Composable
expect fun rememberReadClipboardText(): suspend () -> String?
