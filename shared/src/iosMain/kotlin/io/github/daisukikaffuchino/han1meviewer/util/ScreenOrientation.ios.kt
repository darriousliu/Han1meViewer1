package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import platform.UIKit.UIApplication
import platform.UIKit.UIInterfaceOrientationMask
import platform.UIKit.UIInterfaceOrientationMaskAll
import platform.UIKit.UIInterfaceOrientationMaskLandscape
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindowSceneGeometryPreferencesIOS
import platform.UIKit.setNeedsUpdateOfSupportedInterfaceOrientations

/**
 * 跟播放器全屏同一套：iOS 没有 requestedOrientation，只能请求场景几何更新。
 * 状态栏不用管，iPhone 横屏时系统自己就藏起来了。
 */
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = { isFullscreen ->
    val mask: UIInterfaceOrientationMask =
        if (isFullscreen) UIInterfaceOrientationMaskLandscape else UIInterfaceOrientationMaskAll
    UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?.let { scene ->
            // 先让宿主 controller 重算支持的方向，否则系统会拿旧值去交集
            topMostViewController()?.setNeedsUpdateOfSupportedInterfaceOrientations()
            scene.requestGeometryUpdateWithPreferences(
                UIWindowSceneGeometryPreferencesIOS(mask),
                errorHandler = null,
            )
        }
}

actual val isReportRotationSupported: Boolean = true
