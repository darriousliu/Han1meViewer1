package io.github.daisukikaffuchino.han1meviewer.util

import android.content.ClipData
import android.content.Intent
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.openApplyDeepLinksSettings
import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.han1meviewer.ui.activity.BaseActivity
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_not_support
import io.github.daisukikaffuchino.utils.ActivityManager
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.getDownloadedHanimeVideoUri
import io.github.daisukikaffuchino.utils.startActivitySafely

actual fun restartApplication() = ActivityManager.restart(killProcess = true)

actual val canRestartApplication: Boolean = true

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

actual fun openInExternalPlayer(
    videoUri: String,
    chooserTitle: String,
    onVideoMissing: () -> Unit,
) {
    val externalUri = applicationContext.getDownloadedHanimeVideoUri(videoUri, onVideoMissing)
        ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(externalUri, "video/*")
        clipData = ClipData.newRawUri("video", externalUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, chooserTitle).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (!startActivitySafely(chooser)) SonnerToast.warning(Res.string.action_not_support)
}
