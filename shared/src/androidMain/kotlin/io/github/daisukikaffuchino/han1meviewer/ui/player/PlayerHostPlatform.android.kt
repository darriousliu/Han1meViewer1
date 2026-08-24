package io.github.daisukikaffuchino.han1meviewer.ui.player

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.daisukikaffuchino.utils.OrientationManager

private class AndroidPlayerHost(private val activity: ComponentActivity) : PlayerHostPlatform {
    private var brightnessBeforeFullscreen: Float? = null

    override fun setFullscreen(enabled: Boolean, preferPortrait: Boolean) {
        if (enabled) {
            activity.requestedOrientation = if (preferPortrait) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            setSystemBarsHidden(true)
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            setSystemBarsHidden(false)
            brightnessBeforeFullscreen?.let { overrideBrightness(it) }
            brightnessBeforeFullscreen = null
        }
    }

    private fun setSystemBarsHidden(hidden: Boolean) {
        val controller =
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (hidden) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            activity.window.statusBarColor = Color.BLACK
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            // 显示动画结束后系统可能改回浅色图标，post 一次盖掉
            activity.window.decorView.post {
                WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    override val supportsBrightness: Boolean = true

    override fun currentBrightness(): Float {
        val override = activity.window.attributes.screenBrightness
        if (override >= 0f) return override
        val system = runCatching {
            Settings.System.getInt(activity.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        }.getOrDefault(128)
        return (system / 255f).coerceIn(0f, 1f)
    }

    override fun overrideBrightness(value: Float?) {
        if (value != null && brightnessBeforeFullscreen == null) {
            brightnessBeforeFullscreen = activity.window.attributes.screenBrightness
        }
        activity.window.attributes = activity.window.attributes.apply {
            screenBrightness = value ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
    }

    override fun savedBrightness(): Float? = brightnessBeforeFullscreen

    override fun isInPipMode(): Boolean = activity.isInPictureInPictureMode
}

/** 没有 ComponentActivity 时（理论上不会发生）用它兜底，免得播放页整页白屏。 */
private object NoopPlayerHost : PlayerHostPlatform {
    override fun setFullscreen(enabled: Boolean, preferPortrait: Boolean) = Unit
    override val supportsBrightness: Boolean = false
    override fun currentBrightness(): Float = 1f
    override fun overrideBrightness(value: Float?) = Unit
    override fun savedBrightness(): Float? = null
    override fun isInPipMode(): Boolean = false
}

@Composable
actual fun rememberPlayerHostPlatform(): PlayerHostPlatform {
    val activity = LocalActivity.current as? ComponentActivity
    return remember(activity) { activity?.let(::AndroidPlayerHost) ?: NoopPlayerHost }
}

@Composable
actual fun PlayerWindowEffect(restoreLightSystemBars: Boolean) {
    val activity = LocalActivity.current ?: return
    SideEffect {
        activity.window.statusBarColor = Color.BLACK
        activity.window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        activity.window.isStatusBarContrastEnforced = false
        activity.window.isNavigationBarContrastEnforced = false
    }
    DisposableEffect(activity, restoreLightSystemBars) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.window.statusBarColor = Color.TRANSPARENT
            activity.window.navigationBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                show(WindowInsetsCompat.Type.systemBars())
                isAppearanceLightStatusBars = restoreLightSystemBars
                isAppearanceLightNavigationBars = restoreLightSystemBars
            }
        }
    }
}

@Composable
actual fun PlayerSensorOrientationEffect(
    enabled: Boolean,
    onLandscapeChange: (Boolean) -> Unit,
) {
    val activity = LocalActivity.current ?: return
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(activity, lifecycleOwner, enabled, onLandscapeChange) {
        if (!enabled) return@DisposableEffect onDispose { }
        val manager = OrientationManager(activity) { orientation ->
            onLandscapeChange(orientation.isLandscape)
        }
        lifecycleOwner.lifecycle.addObserver(manager)
        onDispose { lifecycleOwner.lifecycle.removeObserver(manager) }
    }
}
