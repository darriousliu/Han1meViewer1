package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

/** 重启应用，切换站点后需要。 */
expect fun restartApplication()

/** 退出应用（Android 是 finish 当前 Activity）。 */
@Composable
expect fun rememberExitApp(): () -> Unit
