package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

// 桌面端没有 Google Cast，返回 null 让整个投屏分组不渲染
actual fun googleCastAvailability(): Boolean? = null
