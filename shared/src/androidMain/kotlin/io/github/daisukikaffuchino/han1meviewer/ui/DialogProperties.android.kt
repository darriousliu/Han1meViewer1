package io.github.daisukikaffuchino.han1meviewer.ui

import androidx.compose.ui.window.DialogProperties

actual fun fullScreenDialogProperties() = DialogProperties(
    usePlatformDefaultWidth = false,
    decorFitsSystemWindows = false
)