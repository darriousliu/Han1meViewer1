package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

// 桌面端明确不做画中画，设置项按这个标志整个隐藏
internal actual val isPipModeSupported: Boolean = false

internal actual fun isPipPermissionGranted(): Boolean = false

internal actual fun openPipPermissionSettings() = Unit
