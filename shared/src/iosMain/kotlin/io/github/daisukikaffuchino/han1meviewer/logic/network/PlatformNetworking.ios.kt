package io.github.daisukikaffuchino.han1meviewer.logic.network

import platform.Network.nw_path_is_constrained
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.darwin.dispatch_get_main_queue

/**
 * nw_path 只有异步回调、没有同步查询接口，所以常驻一个 monitor 把结果缓存下来，
 * 供 isActiveNetworkMetered() 这类同步调用读取。
 */
internal object DarwinNetworkPath {

    private var monitor: nw_path_monitor_t = null

    /** expensive = 蜂窝/个人热点，constrained = 低数据模式，两者都按计费网络算。 */
    var isMetered: Boolean = false
        private set

    fun start() {
        if (monitor != null) return
        val created = nw_path_monitor_create()
        nw_path_monitor_set_queue(created, dispatch_get_main_queue())
        nw_path_monitor_set_update_handler(created) { path ->
            isMetered = path != null &&
                    (nw_path_is_expensive(path) || nw_path_is_constrained(path))
        }
        nw_path_monitor_start(created)
        monitor = created
    }
}

// TODO(ios): Darwin 侧的代理走 NSURLSession 的配置，还没接
actual fun installPlatformNetworking() {
    DarwinNetworkPath.start()
}

// 没有系统代理可重装
actual fun rebuildPlatformNetworking() = Unit
