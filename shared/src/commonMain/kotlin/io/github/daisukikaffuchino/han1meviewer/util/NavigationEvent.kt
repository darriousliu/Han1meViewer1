package io.github.daisukikaffuchino.han1meviewer.util

import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.HanimeScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * 通常用于非UI触发的导航事件
 */
object NavigationEvent {
    val event: SharedFlow<HanimeScreen>
        field = MutableSharedFlow<HanimeScreen>()

    fun navigation(screen: HanimeScreen) {
        event.tryEmit(screen)
    }
}