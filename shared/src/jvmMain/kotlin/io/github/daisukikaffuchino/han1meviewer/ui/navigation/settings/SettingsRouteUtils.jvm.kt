package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

// TODO Windows hello，Mac的指纹/密码解锁，Linux解锁方式
internal actual fun isDeviceSecureCompat(): Boolean {
    return false
}

// TODO 可能有
internal actual fun isPipPermissionGranted(): Boolean {
    return false
}

// TODO 桌面端应该没有权限这种说法
internal actual fun openPipPermissionSettings() {
}