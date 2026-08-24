package io.github.daisukikaffuchino.han1meviewer.util

actual fun switchLauncherIcon(alias: String) {
}

// 桌面没有「换应用图标」这回事，设置项直接不渲染
actual val isLauncherIconSwitchSupported: Boolean = false
