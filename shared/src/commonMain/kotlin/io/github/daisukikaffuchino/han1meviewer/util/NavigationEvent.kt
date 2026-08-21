package io.github.daisukikaffuchino.han1meviewer.util

import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.HanimeScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * 通常用于非UI触发的导航事件
 */
object NavigationEvent {
    // 必须留缓冲：replay/extraBufferCapacity 都是 0 时，有订阅者的 tryEmit 一定返回
    // false 并丢事件（发射需要挂起）。replay 不能用，否则新订阅者会重放旧路由。
    val event: SharedFlow<HanimeScreen>
        field = MutableSharedFlow<HanimeScreen>(extraBufferCapacity = 8)

    /** @return 是否成功投递；false 表示没有订阅者或缓冲已满，调用方要自己兜底。 */
    fun navigation(screen: HanimeScreen): Boolean = event.tryEmit(screen)
}
