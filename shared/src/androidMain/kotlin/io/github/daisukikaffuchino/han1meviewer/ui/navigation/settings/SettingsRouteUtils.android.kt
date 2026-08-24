package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import android.app.Activity
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_app_open_by_default_settings_not_support
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.startActivitySafely
import kotlin.printStackTrace

internal actual fun isDeviceSecureCompat(): Boolean {
    val km = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return km.isDeviceSecure
}

internal actual val isPipModeSupported: Boolean = true

internal actual fun isPipPermissionGranted(): Boolean {
    val appOps = applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
        Process.myUid(),
        applicationContext.packageName,
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

internal actual fun openPipPermissionSettings() {
    startActivitySafely(
        Intent(
            "android.settings.PICTURE_IN_PICTURE_SETTINGS",
            "package:${applicationContext.packageName}".toUri()
        )
    )
}

@RequiresApi(Build.VERSION_CODES.S)
internal fun openApplyDeepLinksSettings(context: Context, activity: Activity) {
    try {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
            addCategory(Intent.CATEGORY_DEFAULT)
            data = "package:${context.packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        activity.startActivity(intent)
    } catch (e: Exception) {
        SonnerToast.warning(Res.string.action_app_open_by_default_settings_not_support)
        e.printStackTrace()
    }
}