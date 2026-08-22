package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.network.HDns
import io.github.daisukikaffuchino.han1meviewer.logic.network.HProxySelector
import io.github.daisukikaffuchino.han1meviewer.util.monotonicMillis
import java.net.InetAddress

actual suspend fun measureIpDelay(ip: String): Int = runCatching {
    val start = monotonicMillis()
    if (InetAddress.getByName(ip).isReachable(2000)) {
        (monotonicMillis() - start).toInt()
    } else {
        -1
    }
}.getOrDefault(-1)

actual suspend fun resolveCdnIps(host: String): List<String> = HDns().getCDNList(host)

actual suspend fun lookupByDohOnly(host: String): List<String> =
    HDns().lookupByDoHOnly(host).mapNotNull { it.hostAddress }

actual fun applyProxyToSystem() = HProxySelector.rebuildNetwork()
