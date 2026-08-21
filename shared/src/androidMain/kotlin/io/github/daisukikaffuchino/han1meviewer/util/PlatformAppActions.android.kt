package io.github.daisukikaffuchino.han1meviewer.util

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.utils.ActivityManager

actual fun restartApplication() = ActivityManager.restart(killProcess = true)

@Composable
actual fun rememberExitApp(): () -> Unit {
    val activity = LocalActivity.current
    return { activity?.finish() }
}
