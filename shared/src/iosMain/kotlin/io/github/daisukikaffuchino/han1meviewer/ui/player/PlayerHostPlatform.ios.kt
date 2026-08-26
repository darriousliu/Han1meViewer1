package io.github.daisukikaffuchino.han1meviewer.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
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

    /**
     * 不做后台纯音频播放，这里表达的是「进后台别急着按停，画中画可能要接管」。
     *
     * 用「有没有武装」而不是 isInPipMode()：进后台那一刻画中画还没起来，isActive 必然
     * 还是 false，拿它判断会在画中画刚要起来时把播放停掉。武装了但系统最终没起窗口也
     * 无妨——那种情况 iOS 自己会把带画面的播放暂停，正好就是我们想要的「不在后台放」。
     */
    override val playsInBackground: Boolean get() = IosPipTracker.isAutoStartArmed
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
