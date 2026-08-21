package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.network.interceptor.SpeedLimitInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.Protocol
import java.io.File

/**
 * DoH/自定义 DNS、ProxySelector、磁盘缓存、限速这几样 Ktor 没有对应物，
 * 只能配在 OkHttp engine 上，android 和 jvm 共用这一份。
 */
private val httpCache by lazy {
    Cache(
        directory = File((FileKit.cacheDir / "http_cache").absolutePath()),
        maxSize = 10 * 1024 * 1024
    )
}

private val hDns by lazy { HDns() }

internal actual fun createPlatformHttpClient(
    spec: HClientSpec,
    sharedConfig: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient(OkHttp) {
    sharedConfig()
    engine {
        if (spec == HClientSpec.DOWNLOAD) {
            // 限速用的是 okio Throttler，包的是 okhttp 的 ResponseBody，
            // Ktor 的 channel 读到的就是限速后的流
            addInterceptor(SpeedLimitInterceptor(maxSpeed = SettingsRepository.downloadSpeedLimit))
        }
        config {
            dns(hDns)
            when (spec) {
                HClientSpec.HANIME -> {
                    cookieJar(HCookieJar())
                    cache(httpCache)
                    proxySelector(HProxySelector())
                }

                HClientSpec.GETCHU -> {
                    cookieJar(CookieJar.NO_COOKIES)
                    proxySelector(HProxySelector())
                }

                HClientSpec.DOWNLOAD -> protocols(listOf(Protocol.HTTP_1_1))

                HClientSpec.IMAGE -> Unit
            }
        }
    }
}
