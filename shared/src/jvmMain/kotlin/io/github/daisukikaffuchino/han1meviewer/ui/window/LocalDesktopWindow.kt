package io.github.daisukikaffuchino.han1meviewer.ui.window

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow

/**
 * Compose Desktop 没有现成的 LocalWindow，窗口只能从 `Window { }` 的
 * FrameWindowScope 里拿。desktopApp 的 Main 负责提供，播放页全屏要用。
 */
val LocalDesktopWindow = staticCompositionLocalOf<ComposeWindow?> { null }
