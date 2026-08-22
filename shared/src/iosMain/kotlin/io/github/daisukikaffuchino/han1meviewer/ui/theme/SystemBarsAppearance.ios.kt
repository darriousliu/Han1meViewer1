package io.github.daisukikaffuchino.han1meviewer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 系统栏由窗口自己管，不需要额外处理
@Composable
actual fun ApplySystemBarsAppearance(isDark: Boolean, windowBackground: Color) = Unit
