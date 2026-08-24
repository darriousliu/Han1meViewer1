package io.github.daisukikaffuchino.han1meviewer.logic

// TODO Windows hello，Mac 的指纹/密码解锁，Linux 解锁方式
internal actual fun isDeviceSecureCompat(): Boolean = false

// 桌面 JVM 没有跨平台的生物识别 API，isDeviceSecureCompat() 恒为 false，遮罩不会出现
internal actual val canRequestAppUnlock: Boolean = false

internal actual fun requestAppUnlock(reason: String) = Unit
