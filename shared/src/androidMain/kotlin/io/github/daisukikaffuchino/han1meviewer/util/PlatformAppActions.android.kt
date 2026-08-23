package io.github.daisukikaffuchino.han1meviewer.util

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.openApplyDeepLinksSettings
import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.han1meviewer.ui.activity.BaseActivity
import io.github.daisukikaffuchino.utils.ActivityManager

actual fun restartApplication() = ActivityManager.restart(killProcess = true)

@Composable
actual fun rememberExitApp(): () -> Unit {
    val activity = LocalActivity.current
    return { activity?.finish() }
}

@Composable
actual fun rememberSetSecureMode(): ((Boolean) -> Unit)? {
    val activity = LocalActivity.current
    return { enabled -> (activity as? BaseActivity)?.setSecureMode(enabled) }
}

@Composable
actual fun rememberRecreateScreen(): (() -> Unit)? {
    val activity = LocalActivity.current
    return { activity?.recreate() }
}

@Composable
actual fun rememberOpenDeepLinkSettings(): (() -> Unit)? {
    val activity = LocalActivity.current
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || activity == null) return null
    return { openApplyDeepLinksSettings(context, activity) }
}
