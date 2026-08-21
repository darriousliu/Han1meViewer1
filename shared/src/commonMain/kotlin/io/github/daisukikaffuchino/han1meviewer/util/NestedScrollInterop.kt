package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

/** Compose 嵌在 Android View 里时才需要的嵌套滚动互通，其他平台是空实现。 */
@Composable
expect fun rememberNestedScrollInterop(): NestedScrollConnection
