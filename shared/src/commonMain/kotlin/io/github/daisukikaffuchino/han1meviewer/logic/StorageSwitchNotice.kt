package io.github.daisukikaffuchino.han1meviewer.logic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 下载存储位置被动切换时提示用户一次。
 * 触发方在平台侧（Android 是 HCacheManager），消费方是主页面，所以标志放这里。
 */
object StorageSwitchNotice {
    private val _visible = MutableStateFlow(false)
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    fun notifyStorageSwitched() {
        _visible.value = true
    }

    fun dismiss() {
        _visible.value = false
    }
}
