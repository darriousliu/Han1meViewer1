package io.github.daisukikaffuchino.han1meviewer.util

import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/**
 * 取当前用来 present 的 view controller。
 * 走 connectedScenes 而不是已废弃的 UIApplication.keyWindow。
 */
internal fun topMostViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .sortedByDescending { it.activationState == UISceneActivationStateForegroundActive }
        .firstNotNullOfOrNull { scene ->
            scene.windows.filterIsInstance<UIWindow>().firstOrNull { it.keyWindow }
        }
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
