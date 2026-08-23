package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import io.github.daisukikaffuchino.han1meviewer.logic.network.DarwinNetworkPath
import io.github.daisukikaffuchino.han1meviewer.util.topMostViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIInterfaceOrientationMask
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

    // TODO(ios): 屏幕亮度要 UIScreen.mainScreen.brightness，会改全局亮度，先不动
    override fun currentBrightness(): Float = 1f
    override fun overrideBrightness(value: Float?) = Unit
    override fun savedBrightness(): Float? = null

    // TODO(ios): 画中画要 AVPictureInPictureController，等播放内核补上再接
    override fun isInPipMode(): Boolean = false
}

@Composable
actual fun rememberPlayerHostPlatform(): PlayerHostPlatform = IosPlayerHost

@Composable
actual fun PlayerWindowEffect(restoreLightSystemBars: Boolean) {
}

// TODO(ios): 重力感应自动进出全屏，等全屏体验稳定后再接
@Composable
actual fun PlayerSensorOrientationEffect(
    enabled: Boolean,
    onLandscapeChange: (Boolean) -> Unit,
) {
}

// TODO(ios): 画中画,同上
@Composable
actual fun PlayerPipEffect(
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Boolean,
) {
}

// 没有需要动态申请的通知权限
@Composable
actual fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)? = null

actual fun isActiveNetworkMetered(): Boolean = DarwinNetworkPath.isMetered
