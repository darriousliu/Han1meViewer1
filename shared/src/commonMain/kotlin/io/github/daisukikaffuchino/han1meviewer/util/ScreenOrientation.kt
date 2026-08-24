package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

/** 年度报告切换全屏时调整屏幕方向与系统栏。 */
@Composable
expect fun rememberReportWindowMode(): (Boolean) -> Unit
