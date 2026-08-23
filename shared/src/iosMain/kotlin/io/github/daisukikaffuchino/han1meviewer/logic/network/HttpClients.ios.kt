package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.ProxyType
import io.github.daisukikaffuchino.han1meviewer.logic.network.plugin.AttachStoredCookies
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.http.Url

/**
 * NSURLSession 没有 DNS 钩子，DoH/自定义 DNS 做不了（设置页已按平台隐藏）。
 * TODO(iOS): 磁盘缓存与下载限速还没接。
 */
internal actual fun createPlatformHttpClient(
    spec: HClientSpec,
    sharedConfig: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient(Darwin) {
    sharedConfig()
    // Darwin 没有 CookieJar 那一层，登录 cookie 得自己附上去
    if (spec == HClientSpec.HANIME) {
        install(AttachStoredCookies)
        // 同时关掉 NSURLSession 自己的 cookie 处理：登录前匿名浏览留下的会话 cookie
        // 会跟我们附上去的新 cookie 撞在一起，让设置里的那份成为唯一来源。
        // 与 Android 侧 HCookieJar 的语义一致——登录/过盾 cookie 都以设置为准。
        engine { configureSession { setHTTPShouldSetCookies(false) } }
    }
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
