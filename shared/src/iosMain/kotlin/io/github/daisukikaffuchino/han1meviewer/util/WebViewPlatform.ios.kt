package io.github.daisukikaffuchino.han1meviewer.util

import io.github.kdroidfilter.webview.web.NativeWebView
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieStorage
import platform.Foundation.NSURL
import platform.WebKit.WKWebsiteDataStore
import kotlin.coroutines.resume

// WKWebView / wry 默认就开着 DOM storage，不用额外设
actual fun NativeWebView.enableDomStorage() {
}

/**
 * 直接读 WKHTTPCookieStore（全局的，不用经过 [webView]）。
 *
 * 不走 webview 库的 CookieManager：站点下发的是 `.hanime1.me` 这种带前导点的域，
 * 按域名精确匹配会把关键的会话 cookie 漏掉，拿到的串登录不上。
 */
actual suspend fun readWebViewCookies(webView: NativeWebView?, url: String): String? {
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

internal actual suspend fun clearPlatformCookies() {
    val storage = NSHTTPCookieStorage.sharedHTTPCookieStorage
    storage.cookies?.forEach { storage.deleteCookie(it as NSHTTPCookie) }
}
