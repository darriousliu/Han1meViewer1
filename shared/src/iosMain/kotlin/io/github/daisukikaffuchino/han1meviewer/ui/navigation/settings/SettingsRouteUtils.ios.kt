package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

/** 有 Face ID / Touch ID 或设备密码就算「设备是安全的」。 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun isDeviceSecureCompat(): Boolean = memScoped {
    val error = alloc<ObjCObjectVar<NSError?>>()
    LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error.ptr)
}

// TODO iOS也有PIP的播放，等待实现
internal actual fun isPipPermissionGranted(): Boolean {
    return false
}

// TODO 可能有权限
internal actual fun openPipPermissionSettings() {
}