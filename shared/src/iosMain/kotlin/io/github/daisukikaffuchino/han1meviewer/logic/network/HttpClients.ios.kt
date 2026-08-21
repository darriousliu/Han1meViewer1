package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

/**
 * TODO(iOS): NSURLSession 没有 DNS 钩子，DoH/自定义 DNS 做不了；
 *  磁盘缓存与限速也还没接。代理要走 Ktor 的 ProxyBuilder，等设置页的
 *  网络能力门控做好之后再补。
 */
internal actual fun createPlatformHttpClient(
    spec: HClientSpec,
    sharedConfig: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient(Darwin) {
    sharedConfig()
}
