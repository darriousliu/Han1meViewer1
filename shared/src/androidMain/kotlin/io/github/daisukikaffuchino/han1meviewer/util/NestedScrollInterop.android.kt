package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection

@Composable
actual fun rememberNestedScrollInterop(): NestedScrollConnection =
    rememberNestedScrollInteropConnection()
