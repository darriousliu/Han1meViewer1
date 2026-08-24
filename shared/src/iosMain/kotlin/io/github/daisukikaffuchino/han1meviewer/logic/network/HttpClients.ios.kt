package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.ProxyType
import io.github.daisukikaffuchino.han1meviewer.logic.network.plugin.AttachStoredCookies
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.path
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.http.Url
import platform.Foundation.NSURL
import platform.Foundation.NSURLCache
import platform.Foundation.NSURLRequestUseProtocolCachePolicy

/**
 * 主站的磁盘缓存，容量与目录都跟 Android/桌面的 OkHttp `Cache` 对齐，
 * 这样设置页的「清除缓存」（删 cacheDir 下的东西）连它一起清掉。
 *
 * NSURLSession 默认会挂 `NSURLCache.sharedURLCache`，但那份容量和位置都不由我们定，
 * 也不好跟另外两端对口径，所以显式换成自己的。
 */
private val httpCache by lazy {
    NSURLCache(
        memoryCapacity = 4uL * 1024uL * 1024uL,
        diskCapacity = 10uL * 1024uL * 1024uL,
        directoryURL = NSURL.fileURLWithPath((FileKit.cacheDir / "http_cache").path),
    )
}

/**
 * NSURLSession 没有 DNS 钩子，DoH/自定义 DNS 做不了（设置页已按平台隐藏）。
 * 下载限速也做不了：后台会话由系统传输，拿不到字节流（见 NsUrlSessionDownloadController）。
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
        engine {
            configureSession {
                setHTTPShouldSetCookies(false)
                setURLCache(httpCache)
                setRequestCachePolicy(NSURLRequestUseProtocolCachePolicy)
            }
        }
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
