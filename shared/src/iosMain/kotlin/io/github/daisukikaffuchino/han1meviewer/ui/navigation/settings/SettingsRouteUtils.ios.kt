package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.NSError
import platform.AVKit.AVPictureInPictureController
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

/** 有 Face ID / Touch ID 或设备密码就算「设备是安全的」。 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun isDeviceSecureCompat(): Boolean = memScoped {
    val error = alloc<ObjCObjectVar<NSError?>>()
    LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error.ptr)
}

// iPad 上分屏时会返回 false
internal actual val isPipModeSupported: Boolean =
    AVPictureInPictureController.isPictureInPictureSupported()

// iOS 的画中画不需要单独授权，能不能用就看 isPipModeSupported
internal actual fun isPipPermissionGranted(): Boolean = true

internal actual fun openPipPermissionSettings() = Unit