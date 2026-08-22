package io.github.daisukikaffuchino.han1meviewer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 系统动态取色的主色。平台不支持（含 Android 12 以下）时返回 null，
 * 调用方据此回落到内置配色，「动态取色」这类设置项也应据此隐藏。
 */
@Composable
expect fun dynamicAccentColorOrNull(): Color?
