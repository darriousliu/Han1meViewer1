package io.github.daisukikaffuchino.utils

import androidx.compose.runtime.Composable

/** 复制文本到系统剪贴板。 */
@Composable
expect fun rememberCopyTextToClipboard(): (CharSequence) -> Unit

/** 调起系统分享。 */
@Composable
expect fun rememberShareText(): (CharSequence, CharSequence?) -> Unit
