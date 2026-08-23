package io.github.daisukikaffuchino.han1meviewer.util

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSURL
import platform.WebKit.WKWebsiteDataStore
import kotlin.coroutines.resume

/**
 * 直接读 WKHTTPCookieStore。
 *
 * 不走 webview 库的 CookieManager：站点下发的是 `.hanime1.me` 这种带前导点的域，
 * 按域名精确匹配会把关键的会话 cookie 漏掉，拿到的串登录不上。
 */
actual suspend fun readWebViewCookies(url: String): String? {
    val host = NSURL(string = url).host ?: return null
    return suspendCancellableCoroutine { continuation ->
        WKWebsiteDataStore.defaultDataStore().httpCookieStore.getAllCookies { cookies ->
            val matched = cookies.orEmpty()
                .filterIsInstance<NSHTTPCookie>()
                .filter { host.matchesCookieDomain(it.domain) }
                .joinToString("; ") { "${it.name}=${it.value}" }
            continuation.resume(matched)
        }
    }
}

/** cookie 的域可能带前导点表示「含子域」，`hanime1.me` 要能命中 `.hanime1.me`。 */
private fun String.matchesCookieDomain(domain: String?): Boolean {
    val bare = domain?.trimStart('.')?.lowercase().orEmpty()
    if (bare.isEmpty()) return false
    val host = lowercase()
    return host == bare || host.endsWith(".$bare")
}
