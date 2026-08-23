package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * 外部入口的投递通道。平台壳把自己那套（Android 的 Intent、iOS 的 Universal Links、
 * 桌面的命令行参数）解析成 [DeepLinkTarget] 投进来，App() 收了往返回栈上推。
 *
 * replay = 1：冷启动时 Intent 先到、组合后到，不留一份的话首次跳转会丢。
 */
object DeepLinkBus {
    val targets: SharedFlow<DeepLinkTarget>
        field = MutableSharedFlow<DeepLinkTarget>(
            replay = 1,
            extraBufferCapacity = 1,
        )

    fun post(target: DeepLinkTarget) {
        targets.tryEmit(target)
    }
}
