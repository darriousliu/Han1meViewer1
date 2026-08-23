package io.github.daisukikaffuchino.han1meviewer.logic

import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

internal actual val canRequestAppUnlock: Boolean = true

/**
 * DeviceOwnerAuthentication = Face ID / Touch ID，失败或没录入时回落到设备密码。
 * iOS 不能像 Android 那样鉴权失败就退出应用，所以失败时保持遮罩，让用户点一下重试。
 */
internal actual fun requestAppUnlock(reason: String) {
    LAContext().evaluatePolicy(LAPolicyDeviceOwnerAuthentication, reason) { success, _ ->
        if (success) {
            dispatch_async(dispatch_get_main_queue()) { AppLockGuard.onAuthenticated() }
        }
    }
}
