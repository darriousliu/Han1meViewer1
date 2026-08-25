package io.github.daisukikaffuchino.han1meviewer.util

import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable

@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit {
    val activity = LocalActivity.current
    return { isFullscreen ->
        activity?.apply {
            requestedOrientation = if (isFullscreen) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
                window.insetsController?.apply {
                    if (isFullscreen) {
                        hide(bars)
                        systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        show(bars)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                run {
                    window.decorView.systemUiVisibility = if (isFullscreen) {
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN
                    } else {
                        View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }
            }
        }
    }
}

actual val isReportRotationSupported: Boolean = true
