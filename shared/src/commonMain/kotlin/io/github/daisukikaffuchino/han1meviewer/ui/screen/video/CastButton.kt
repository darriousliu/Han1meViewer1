package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 投屏按钮。Google Cast 是 Android 专属能力，其他平台什么都不画，
 * 调用方不需要自己判断平台。
 */
@Composable
expect fun CastButton(modifier: Modifier = Modifier)
