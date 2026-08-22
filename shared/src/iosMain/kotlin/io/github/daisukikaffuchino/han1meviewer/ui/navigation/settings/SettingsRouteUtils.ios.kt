package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

// TODO iOS面容解锁之类的判断
internal actual fun isDeviceSecureCompat(): Boolean {
    return false
}

// TODO iOS也有PIP的播放，等待实现
internal actual fun isPipPermissionGranted(): Boolean {
    return false
}

// TODO 可能有权限
internal actual fun openPipPermissionSettings() {
}