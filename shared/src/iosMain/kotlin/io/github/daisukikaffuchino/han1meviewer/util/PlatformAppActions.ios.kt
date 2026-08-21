package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

// TODO(iOS): iOS 不允许应用自行重启/退出，这两个操作留空
actual fun restartApplication() = Unit

@Composable
actual fun rememberExitApp(): () -> Unit = {}
