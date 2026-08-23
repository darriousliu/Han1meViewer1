package io.github.daisukikaffuchino.han1meviewer.logic

import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.isDeviceSecureCompat
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
