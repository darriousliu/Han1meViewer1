package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Rect
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.ui.bridge.CurrentVideoHost
import io.github.daisukikaffuchino.han1meviewer.ui.bridge.VideoPageHost
import io.github.daisukikaffuchino.utils.OrientationManager
import io.github.daisukikaffuchino.utils.applicationContext
import kotlin.math.roundToInt
import io.github.daisukikaffuchino.han1meviewer.ui.bridge.ACTION_TOGGLE_PLAY

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

// 两个状态用不同的 requestCode：requestCode 相同的话 getBroadcast 返回的是同一个
// PendingIntent，SystemUI 会认为 RemoteAction 没变，图标不刷新（HyperOS 上实测如此）
private const val PIP_REQUEST_PLAY = 1
private const val PIP_REQUEST_PAUSE = 2

private fun Activity.pipTogglePlayIntent(isPlaying: Boolean): PendingIntent =
    PendingIntent.getBroadcast(
        this,
        if (isPlaying) PIP_REQUEST_PAUSE else PIP_REQUEST_PLAY,
        Intent(ACTION_TOGGLE_PLAY).setPackage(packageName),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

private fun Activity.pipParams(
    isPlaying: Boolean,
    extra: PictureInPictureParams.Builder.() -> Unit = {},
): PictureInPictureParams = PictureInPictureParams.Builder()
    .setAspectRatio(Rational(16, 9))
    .setActions(listOf(pipPlayPauseAction(isPlaying)))
    .apply(extra)
    .build()

private fun Activity.updatePipActions(isPlaying: Boolean) {
    if (!isInPictureInPictureMode) return
    setPictureInPictureParams(pipParams(isPlaying))
}

private fun Activity.pipPlayPauseAction(isPlaying: Boolean): RemoteAction = RemoteAction(
    Icon.createWithResource(
        this,
        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow,
    ),
    getString(R.string.play_pause),
    getString(R.string.play_pause),
    pipTogglePlayIntent(isPlaying),
)

@Composable
actual fun PlayerPipEffect(
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Boolean,
) {
    val activity = LocalActivity.current ?: return
    // 全部走 rememberUpdatedState：host 只按 activity 注册一次，且内部读到的永远是最新值。
    // 之前把这些做成 DisposableEffect 的 key，host 里捕获的是旧快照，
    // 点完画中画的按钮刷新出来的还是切换前的图标。
    val currentShouldEnterPip by rememberUpdatedState(shouldEnterPip)
    val currentIsPlaying by rememberUpdatedState(isPlaying)
    val currentBounds by rememberUpdatedState(sourceBounds)
    val currentOnPipModeChanged by rememberUpdatedState(onPipModeChanged)
    val currentOnToggle by rememberUpdatedState(onTogglePlayPause)

    LaunchedEffect(activity, isPlaying) {
        activity.updatePipActions(isPlaying)
    }

    DisposableEffect(activity) {
        val host = object : VideoPageHost {
            override fun shouldEnterPip(): Boolean = currentShouldEnterPip()

            override fun enterPipMode() {
                activity.enterPictureInPictureMode(
                    activity.pipParams(currentIsPlaying) {
                        currentBounds()?.let { b ->
                            setSourceRectHint(
                                android.graphics.Rect(
                                    b.left.roundToInt(), b.top.roundToInt(),
                                    b.right.roundToInt(), b.bottom.roundToInt(),
                                )
                            )
                        }
                    }
                )
            }

            override fun onPipModeChanged(isInPip: Boolean) {
                currentOnPipModeChanged(isInPip)
                if (isInPip) activity.updatePipActions(currentIsPlaying)
            }

            override fun togglePlayPause() {
                // 引擎状态是异步回来的，这里先按返回值刷一次，
                // 上面的 LaunchedEffect 会在状态真正落地后再校正一次
                activity.updatePipActions(currentOnToggle())
            }
        }
        CurrentVideoHost.register(host)
        onDispose { CurrentVideoHost.register(null) }
    }
}

@Composable
actual fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (!granted) onDenied() }
    return {
        val granted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

actual fun isActiveNetworkMetered(): Boolean = runCatching {
    val cm = applicationContext.getSystemService(ConnectivityManager::class.java)
    cm?.isActiveNetworkMetered == true
}.getOrDefault(false)
