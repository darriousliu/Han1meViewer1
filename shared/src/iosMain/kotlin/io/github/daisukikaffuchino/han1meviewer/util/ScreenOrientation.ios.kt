package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

// TODO(iOS): 锁横屏 + 隐藏状态栏要动 UIViewController，跟播放器全屏一起做
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = {}
