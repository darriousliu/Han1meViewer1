package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.ProxyType
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.http.Url

/**
 * TODO(iOS): NSURLSession 没有 DNS 钩子，DoH/自定义 DNS 做不了（设置页已按平台隐藏）；
 *  磁盘缓存与限速也还没接。
 */
internal actual fun createPlatformHttpClient(
    spec: HClientSpec,
    sharedConfig: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient(Darwin) {
    sharedConfig()
    // Direct 时留空：NSURLSession 默认就跟随系统代理，Ktor 的 proxy = null 也是这个语义，
    // 想强制直连得清掉 connectionProxyDictionary
    val proxy = currentProxyConfig()
    if (proxy != null) {
        engine { this.proxy = proxy }
    } else if (SettingsRepository.current.proxyType == ProxyType.Direct) {
        engine { configureSession { setConnectionProxyDictionary(emptyMap<Any?, Any?>()) } }
    }
}

private fun currentProxyConfig(): ProxyConfig? {
    val settings = SettingsRepository.current
    val host = settings.proxyIp
    val port = settings.proxyPort
    if (host.isBlank() || port !in 1..65535) return null
    return when (settings.proxyType) {
        ProxyType.Http -> ProxyBuilder.http(Url("http://$host:$port"))
        ProxyType.Socks -> ProxyBuilder.socks(host, port)
        ProxyType.Direct, ProxyType.System -> null
    }
}
