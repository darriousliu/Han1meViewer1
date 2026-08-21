package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.DESKTOP_USER_AGENT
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT
import io.github.daisukikaffuchino.han1meviewer.logic.network.plugin.UrlLogging
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

/** 四个 client 的差异只有超时和请求头，engine 侧的差异交给 actual。 */
enum class HClientSpec(val connectTimeoutMillis: Long) {
    /** 主站：cookie + 过盾 + 磁盘缓存 + 代理 */
    HANIME(15_000L),

    /** getchu：固定请求头，不带 cookie */
    GETCHU(15_000L),

    /** 视频下载：HTTP/1.1 + 限速，不带 cookie/缓存 */
    DOWNLOAD(5_000L),

    /** 图片：只要自定义 DNS */
    IMAGE(5_000L),
}

/**
 * 整个 HttpClient（不只是 engine）由平台构造：这样 rebuildHttpClient() 里的 close()
 * 才真的能把连接池释放掉。
 */
internal expect fun createPlatformHttpClient(
    spec: HClientSpec,
    sharedConfig: HttpClientConfig<*>.() -> Unit,
): HttpClient

internal fun buildHttpClient(spec: HClientSpec): HttpClient = createPlatformHttpClient(spec) {
    expectSuccess = false
    install(HttpTimeout) { connectTimeoutMillis = spec.connectTimeoutMillis }
    when (spec) {
        HClientSpec.HANIME -> {
            install(UrlLogging)
            installCloudflareChallenge()
            defaultRequest { header(HttpHeaders.UserAgent, USER_AGENT) }
        }

        HClientSpec.GETCHU -> {
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
        }

        HClientSpec.DOWNLOAD -> defaultRequest { header(HttpHeaders.UserAgent, USER_AGENT) }

        HClientSpec.IMAGE -> Unit
    }
}

object ServiceCreator {
    var hClient: HttpClient = buildHttpClient(HClientSpec.HANIME)
        private set

    var getchuClient: HttpClient = buildHttpClient(HClientSpec.GETCHU)
        private set

    var downloadClient: HttpClient = buildHttpClient(HClientSpec.DOWNLOAD)
        private set

    var imageClient: HttpClient = buildHttpClient(HClientSpec.IMAGE)
        private set

    /** 切镜像/代理/DNS 之后重建，旧的要 close 掉释放连接池。 */
    fun rebuildHttpClient() {
        hClient.close()
        getchuClient.close()
        hClient = buildHttpClient(HClientSpec.HANIME)
        getchuClient = buildHttpClient(HClientSpec.GETCHU)
    }
}
