package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

// TODO Windows hello，Mac的指纹/密码解锁，Linux解锁方式
internal actual fun isDeviceSecureCompat(): Boolean {
    return false
}

// 桌面端明确不做画中画，设置项按这个标志整个隐藏
internal actual val isPipModeSupported: Boolean = false

internal actual fun isPipPermissionGranted(): Boolean = false

internal actual fun openPipPermissionSettings() = Unit