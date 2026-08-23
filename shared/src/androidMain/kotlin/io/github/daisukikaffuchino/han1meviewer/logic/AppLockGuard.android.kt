package io.github.daisukikaffuchino.han1meviewer.logic

// 鉴权由 MainActivity 驱动：失败要 finish 掉 Activity，组合层做不到
internal actual val canRequestAppUnlock: Boolean = false

internal actual fun requestAppUnlock(reason: String) = Unit
