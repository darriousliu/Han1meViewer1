package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

// TODO(ios): 节点延迟 / DoH 测试 / 系统代理都依赖 OkHttp 那套，iOS 还没有对应实现
actual suspend fun measureIpDelay(ip: String): Int = -1

actual suspend fun resolveCdnIps(host: String): List<String> = emptyList()

actual suspend fun lookupByDohOnly(host: String): List<String> = emptyList()

actual fun applyProxyToSystem() {
}
