package io.github.daisukikaffuchino.han1meviewer.util

import io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.kdroidfilter.webview.web.NativeWebView
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.CookieHandler
import java.net.CookieManager

// WKWebView / wry 默认就开着 DOM storage，不用额外设
actual fun NativeWebView.enableDomStorage() {
}

/**
 * 从 wry 的 cookie 存储里取全量 cookie，自己按域过滤。
 *
 * 不走 webview 库的 CookieManager：它底下是 wry 0.54 的 `cookies_for_url`，过滤条件是
 * `cookie.domain() == url.domain()` ——**字符串全等**，没有 RFC 6265 的后缀匹配，
 * 站点下发到 `.hanime1.me` 的 cookie（登录态、`cf_clearance` 都在这一档）在任何子域下都取不到。
 * 这里改成取全量再按 [matchesCookieDomain] 过滤，跟 iOS 同一套口径。
 *
 * 取 cookie 的原生调用在后台线程上发（库自己的 CookieManager 也是这么做的）。注意 wry 那条路
 * 有 1 秒硬超时，而且它等结果的办法是在 `NSRunLoop.mainRunLoop()` 上 `acceptInputForMode:`
 * ——那个只在主线程上有效，从后台线程调等于空转，所以读空是可能发生的偶发事件，
 * 调用方要能重试（过盾页是按秒轮询的）。
 */
actual suspend fun readWebViewCookies(webView: NativeWebView?, url: String): String? {
    val panel = webView ?: return null
    val host = runCatching { Url(url).host }.getOrNull()?.takeIf { it.isNotEmpty() } ?: return null
    return withContext(Dispatchers.IO) {
        runCatching {
            panel.getCookies()
                .filter { host.matchesCookieDomain(it.domain) }
                .joinToString("; ") { "${it.name}=${it.value}" }
        }.getOrElse {
            LogUtil.e("WebViewPlatform", "读取 WebView cookie 失败", it)
            null
        }
        // 空串按「没取到」算，让调用方还能回落到库自带的 CookieManager
    }?.takeIf { it.isNotEmpty() }
}

internal actual suspend fun clearPlatformCookies() {
    HCookieJar.cookieMap.clear()
    (CookieHandler.getDefault() as? CookieManager)?.cookieStore?.removeAll()
}
