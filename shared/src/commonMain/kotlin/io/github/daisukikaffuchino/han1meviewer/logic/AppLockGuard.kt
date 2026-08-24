package io.github.daisukikaffuchino.han1meviewer.logic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 应用锁遮罩。放在这里而不是 HomePageViewModel 里，是为了让平台壳不必持有 ViewModel
 */
object AppLockGuard {
    val visible: StateFlow<Boolean>
        field = MutableStateFlow(SettingsRepository.current.useLockScreen && isDeviceSecureCompat())

    /** 平台侧鉴权通过后调用。 */
    fun onAuthenticated() {
        visible.value = false
    }
}

/** 设备有没有设锁屏 / 生物识别；没有的话应用锁整个不生效。 */
internal expect fun isDeviceSecureCompat(): Boolean

/**
 * 遮罩起来后能不能由组合层自己发起鉴权。
 *
 * Android 是 MainActivity 在 onCreate 里驱动 BiometricPrompt 的（失败要 finish 掉
 * Activity，组合层做不到），所以那边是 false。
 */
internal expect val canRequestAppUnlock: Boolean

/** 发起一次系统鉴权，通过后自行调用 [AppLockGuard.onAuthenticated]。 */
internal expect fun requestAppUnlock(reason: String)
