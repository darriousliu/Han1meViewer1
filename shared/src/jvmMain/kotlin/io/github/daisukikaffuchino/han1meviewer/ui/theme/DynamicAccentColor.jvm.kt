package io.github.daisukikaffuchino.han1meviewer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 没有系统动态取色
@Composable
actual fun dynamicAccentColorOrNull(): Color? = null

actual fun isDynamicColorSupported(): Boolean = false
