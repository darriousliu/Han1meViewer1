package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.network.resolveAddresses
import io.github.daisukikaffuchino.han1meviewer.logic.network.tcpConnectMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private const val PROBE_PORT = 443
private const val PROBE_TIMEOUT_MILLIS = 2000

actual suspend fun measureIpDelay(ip: String): Int = withContext(Dispatchers.IO) {
    tcpConnectMillis(ip, PROBE_PORT, PROBE_TIMEOUT_MILLIS)
}

/** iOS 走系统 DNS，没有自定义 hosts 那条分支。 */
actual suspend fun resolveCdnIps(host: String): List<String> = withContext(Dispatchers.IO) {
    resolveAddresses(host)
}

// DoH 改不了 NSURLSession 的解析，入口已按平台隐藏，这里不做
actual suspend fun lookupByDohOnly(host: String): List<String> = emptyList()

// iOS 没有「系统代理」，代理是配在 Darwin 引擎上的，调用方随后的
// HanimeNetwork.rebuildNetwork() 会重建 client 让它生效
actual fun applyProxyToSystem() = Unit

actual val isCustomDnsSupported: Boolean = false
