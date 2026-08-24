package io.github.daisukikaffuchino.han1meviewer.util

/** 换应用图标：Android 是 activity-alias，iOS 是备用图标，桌面没有这回事。 */
expect fun switchLauncherIcon(alias: String)

expect val isLauncherIconSwitchSupported: Boolean
