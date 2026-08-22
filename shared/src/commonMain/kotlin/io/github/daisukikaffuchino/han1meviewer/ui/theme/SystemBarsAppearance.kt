package io.github.daisukikaffuchino.han1meviewer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 主题切换时同步系统栏外观（状态栏/导航栏图标明暗、窗口背景）。
 * 只有 Android 需要，其他平台由窗口自身管理。
 */
@Composable
expect fun ApplySystemBarsAppearance(isDark: Boolean, windowBackground: Color)
