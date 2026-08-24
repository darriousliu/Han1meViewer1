package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.network.DarwinNetworkPath
import io.github.daisukikaffuchino.han1meviewer.ui.player.ComposeMediaPlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.IosPipTracker
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.util.topMostViewController
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.UIKit.UIInterfaceOrientationMask
import platform.UIKit.UIScreen
import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIInterfaceOrientationMaskLandscape
import platform.UIKit.UIInterfaceOrientationMaskPortrait
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindowSceneGeometryPreferencesIOS
import platform.UIKit.setNeedsUpdateOfSupportedInterfaceOrientations

private object IosPlayerHost : PlayerHostPlatform {

    /**
     * iOS 没有 Android 那种 requestedOrientation，只能请求场景几何更新。
     * 状态栏不用管：iPhone 横屏时系统自己就藏起来了。
     */
    override fun setFullscreen(enabled: Boolean, preferPortrait: Boolean) {
        val mask: UIInterfaceOrientationMask = when {
            !enabled -> UIInterfaceOrientationMaskAll
            preferPortrait -> UIInterfaceOrientationMaskPortrait
            else -> UIInterfaceOrientationMaskLandscape
        }
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: return
        // 先让宿主 controller 重算支持的方向，否则系统会拿旧值去交集
        topMostViewController()?.setNeedsUpdateOfSupportedInterfaceOrientations()
        scene.requestGeometryUpdateWithPreferences(
            UIWindowSceneGeometryPreferencesIOS(mask),
            errorHandler = null,
        )
    }

    /**
     * iOS 没有「窗口亮度」，只有全局屏幕亮度，改了会影响整个系统，
     * 所以退出全屏时必须还原成进来之前那档。
     */
    override val supportsBrightness: Boolean = true

    private var brightnessBeforeOverride: Float? = null

    override fun currentBrightness(): Float =
        UIScreen.mainScreen.brightness.toFloat().coerceIn(0f, 1f)

    override fun overrideBrightness(value: Float?) {
        if (value != null && brightnessBeforeOverride == null) {
            brightnessBeforeOverride = currentBrightness()
        }
        val target = value ?: brightnessBeforeOverride ?: return
        UIScreen.mainScreen.brightness = target.toDouble()
        if (value == null) brightnessBeforeOverride = null
    }

    override fun savedBrightness(): Float? = brightnessBeforeOverride

    override fun isInPipMode(): Boolean = IosPipTracker.isActive

    // Info.plist 里开了 UIBackgroundModes=audio，退到后台只留音频继续放
    override val playsInBackground: Boolean = true
}

@Composable
actual fun rememberPlayerHostPlatform(): PlayerHostPlatform = IosPlayerHost

@Composable
actual fun PlayerWindowEffect(restoreLightSystemBars: Boolean) {
}

/**
 * 听设备（不是界面）的物理方向，跟 Android 的 OrientationEventListener 同语义：
 * 全屏时界面被我们锁成了横屏，只有物理方向还能反映用户把手机转回竖着了。
 *
 * iOS 没有公开 API 能读「屏幕旋转锁定」，所以这里比 Android 少一层判断——
 * 锁了旋转照样会自动进出全屏，这也是 iOS 上视频类应用的普遍做法。
 */
@Composable
actual fun PlayerSensorOrientationEffect(
    enabled: Boolean,
    onLandscapeChange: (Boolean) -> Unit,
) {
    val currentOnLandscapeChange by rememberUpdatedState(onLandscapeChange)
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }
        val device = UIDevice.currentDevice
        // 不开这个 orientation 恒为 Unknown
        device.beginGeneratingDeviceOrientationNotifications()
        var lastLandscape: Boolean? = null
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(
            UIDeviceOrientationDidChangeNotification,
            null,
            NSOperationQueue.mainQueue,
        ) { _ ->
            val landscape = when (device.orientation) {
                UIDeviceOrientation.UIDeviceOrientationLandscapeLeft,
                UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> true

                UIDeviceOrientation.UIDeviceOrientationPortrait,
                UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown -> false

                // 平放/翻过来/未知：拿不到有效方向，别动
                else -> null
            }
            if (landscape != null && landscape != lastLandscape) {
                lastLandscape = landscape
                currentOnLandscapeChange(landscape)
            }
        }
        onDispose {
            center.removeObserver(observer)
            device.endGeneratingDeviceOrientationNotifications()
        }
    }
}

/**
 * 画中画交给 composemediaplayer（见库的 README_VIDEO）：
 * `isPipEnabled = true` 就是「应用退到后台时自动起画中画」，底下还是
 * AVPictureInPictureController，我们不用自己再建一个。
 *
 * iOS 没有 Android 那种 onUserLeaveHint 时机可挑，条件只能提前推给库、由系统决定，
 * 所以条件变化时要重新推一次。
 *
 * 画中画窗口上的播放/暂停由系统直接驱动 AVPlayer，[onTogglePlayPause] 和
 * [sourceBounds]（起始动画由系统按图层算）在这一端都用不上。
 */
@Composable
actual fun PlayerPipEffect(
    engine: PlaybackEngine?,
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Boolean,
) {
    val mediaEngine = engine as? ComposeMediaPlaybackEngine ?: return
    val playerState = mediaEngine.player
    val appSettings by SettingsRepository.settings.collectAsStateWithLifecycle()
    // shouldEnterPip() 读的是 PlaybackEngine.state 这个 StateFlow 的 value，不是
    // Compose state，本身不构成订阅；而这个 composable 的参数都是稳定的、又没读别的
    // 会变的状态，于是被 Compose 整个跳过重组——结论会永远停在第一次组合时（那时还
    // 没开始播）算出的 false，画中画永远武装不上。所以这里显式订阅引擎状态。
    val engineState by mediaEngine.state.collectAsStateWithLifecycle()
    val autoStart = remember(engineState, appSettings.allowPipMode, playerState) {
        playerState.isPipSupported && appSettings.allowPipMode && shouldEnterPip()
    }
    LaunchedEffect(playerState, autoStart) {
        playerState.isPipEnabled = autoStart
        IosPipTracker.isAutoStartArmed = autoStart
    }
    DisposableEffect(playerState) {
        // 后台音频那条路要现读画中画状态，把 playerState 交给它
        IosPipTracker.playerState = playerState
        onDispose {
            playerState.isPipEnabled = false
            IosPipTracker.isAutoStartArmed = false
            IosPipTracker.playerState = null
        }
    }

    // isPipActive 是库维护的 Compose State，读它就能知道用户把画中画关掉了，
    // 不需要自己挂 AVPictureInPictureControllerDelegate。
    // 注意这条只在前台有效：退到后台后 Compose 不再重组，所以别处要用的画中画状态
    // 一律走 IosPipTracker 现读，不能靠这里同步。
    val pipActive = playerState.isPipActive
    LaunchedEffect(pipActive) { onPipModeChanged(pipActive) }
}

// 没有需要动态申请的通知权限
@Composable
actual fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)? = null

actual fun isActiveNetworkMetered(): Boolean = DarwinNetworkPath.isMetered
