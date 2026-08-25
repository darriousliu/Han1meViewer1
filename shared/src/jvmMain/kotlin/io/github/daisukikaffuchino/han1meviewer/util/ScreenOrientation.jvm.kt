package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

// 桌面窗口宽大于高，年度报告不需要转向，按钮也不出现
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = {}

actual val isReportRotationSupported: Boolean = false
