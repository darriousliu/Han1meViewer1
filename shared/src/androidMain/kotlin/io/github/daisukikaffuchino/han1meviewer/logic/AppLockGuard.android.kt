package io.github.daisukikaffuchino.han1meviewer.logic

import android.app.KeyguardManager
import android.content.Context
import io.github.daisukikaffuchino.utils.applicationContext

internal actual fun isDeviceSecureCompat(): Boolean {
    val km = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return km.isDeviceSecure
}

// 鉴权由 MainActivity 驱动：失败要 finish 掉 Activity，组合层做不到
internal actual val canRequestAppUnlock: Boolean = false

internal actual fun requestAppUnlock(reason: String) = Unit
