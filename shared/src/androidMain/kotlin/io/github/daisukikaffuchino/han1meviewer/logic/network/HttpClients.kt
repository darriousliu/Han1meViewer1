package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.DESKTOP_USER_AGENT
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.network.interceptor.SpeedLimitInterceptor
import io.github.daisukikaffuchino.han1meviewer.logic.network.plugin.CloudflareChallenge
import io.github.daisukikaffuchino.han1meviewer.logic.network.plugin.UrlLogging
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.unsafeLazy
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * DoH/自定义 DNS、ProxySelector、磁盘缓存、限速这几样 Ktor 没有对应物，
 * 只能配在 OkHttp engine 上（Ktor 3.5 的 dns 属性也只是把 OkHttpConfig.dns 暴露出来，
 * 且 Darwin 不支持）。往 iOS 铺的时候这一份由 android/jvm 共用，Darwin 另写一份。
 */
object ServiceCreator {

    private val cache = Cache(
        directory = File(applicationContext.cacheDir, "http_cache"),
        maxSize = 10 * 1024 * 1024
    )

    private val downloadSpeedLimitInterceptor by unsafeLazy {
        SpeedLimitInterceptor(maxSpeed = SettingsRepository.downloadSpeedLimit)
    }

    private val hDns = HDns()

    var hClient: HttpClient = buildHClient()
        private set

    var getchuClient: HttpClient = buildGetchuClient()
        private set

    /** 下载走裸 OkHttp：断点续传/Range/进度回调那套还没搬到 Ktor */
    var downloadClient: OkHttpClient = buildDownloadClient()
        private set

    fun rebuildHttpClient() {
        hClient.close()
        getchuClient.close()
        hClient = buildHClient()
        getchuClient = buildGetchuClient()
    }

    private fun buildHClient() = HttpClient(OkHttp) {
        expectSuccess = false
        install(HttpTimeout) { connectTimeoutMillis = 15_000 }
        install(UrlLogging)
        install(CloudflareChallenge)
        defaultRequest { header(HttpHeaders.UserAgent, USER_AGENT) }
        engine {
            config {
                cookieJar(HCookieJar())
                cache(cache)
                proxySelector(HProxySelector())
                dns(hDns)
            }
        }
    }

    private fun buildGetchuClient() = HttpClient(OkHttp) {
        expectSuccess = false
        install(HttpTimeout) { connectTimeoutMillis = 15_000 }
        install(UrlLogging)
        defaultRequest {
            header(HttpHeaders.UserAgent, DESKTOP_USER_AGENT)
            header(HttpHeaders.Referrer, "https://www.getchu.com/")
            header(HttpHeaders.Cookie, "getchu_adalt_flag=getchu.com; gc=gc")
            header(
                HttpHeaders.Accept,
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            )
            header(HttpHeaders.AcceptLanguage, "ja,en-US;q=0.9,en;q=0.8")
            header(HttpHeaders.CacheControl, "no-cache")
        }
        engine {
            config {
                cookieJar(CookieJar.NO_COOKIES)
                proxySelector(HProxySelector())
                dns(hDns)
            }
        }
    }

    private fun buildDownloadClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1))
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().addHeader("User-Agent", USER_AGENT).build()
            )
        }
        .addInterceptor(downloadSpeedLimitInterceptor)
        .dns(hDns)
        .build()
}
