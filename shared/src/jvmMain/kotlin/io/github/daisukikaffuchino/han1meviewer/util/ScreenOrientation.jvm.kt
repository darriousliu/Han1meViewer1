package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

// TODO(JVM): 窗口全屏要拿到 desktopApp 的 WindowState，跟播放器全屏一起做
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = {}
