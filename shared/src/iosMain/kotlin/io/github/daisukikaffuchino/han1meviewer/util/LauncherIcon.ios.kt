package io.github.daisukikaffuchino.han1meviewer.util

import io.github.daisukikaffuchino.han1meviewer.logic.model.LauncherIconOption
import platform.UIKit.UIApplication
import platform.UIKit.alternateIconName
import platform.UIKit.setAlternateIconName
import platform.UIKit.supportsAlternateIcons

/** 备用图标在 Assets.xcassets 里，由 ASSETCATALOG_COMPILER_ALTERNATE_APPICON_NAMES 打进包。 */
actual fun switchLauncherIcon(alias: String) {
    val application = UIApplication.sharedApplication
    if (!application.supportsAlternateIcons) return
    val target = LauncherIconOption.fromAlias(alias).iosIconName
    if (application.alternateIconName == target) return
    // 必须在主线程调；调用方是 rememberCoroutineScope()，默认就在主线程
    application.setAlternateIconName(target, null)
}

actual val isLauncherIconSwitchSupported: Boolean = true
