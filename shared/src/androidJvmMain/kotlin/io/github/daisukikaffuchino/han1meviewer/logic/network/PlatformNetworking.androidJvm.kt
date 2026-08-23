package io.github.daisukikaffuchino.han1meviewer.logic.network

import java.net.ProxySelector

actual fun installPlatformNetworking() {
    ProxySelector.setDefault(HProxySelector())
    HProxySelector.rebuildNetwork()
}

actual fun rebuildPlatformNetworking() {
    HProxySelector.rebuildNetwork()
}
