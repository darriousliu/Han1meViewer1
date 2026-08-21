package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

// 没有 View 体系，不需要互通
@Composable
actual fun rememberNestedScrollInterop(): NestedScrollConnection =
    remember { object : NestedScrollConnection {} }
