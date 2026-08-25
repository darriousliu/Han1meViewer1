package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

/** 年度报告切换全屏时调整屏幕方向与系统栏。 */
@Composable
expect fun rememberReportWindowMode(): (Boolean) -> Unit

/**
 * 这个平台的年度报告要不要给「转横屏」按钮。
 *
 * 桌面窗口本来就是宽大于高，转不了也没必要转，按钮整个不出现。
 */
expect val isReportRotationSupported: Boolean
