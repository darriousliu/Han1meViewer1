package io.github.daisukikaffuchino.han1meviewer.util

import io.github.kdroidfilter.webview.web.NativeWebView

/**
 * Android 的 WebView 默认不开 DOM storage，而 Cloudflare 过盾页要用 localStorage。
 * 这一版 webview 库的 AndroidWebSettings 没暴露这个开关，只能从 onCreated 拿原生对象设。
 */
expect fun NativeWebView.enableDomStorage()

/**
 * 直接从平台的 WebView cookie 存储里取该域下的 cookie，拼成 `a=1; b=2`。
 *
 * [webView] 是 `WebView(onCreated = ...)` 回调给出的原生对象，桌面端只能顺着它才够得到
 * cookie 存储；还没建好时传 null。
 *
 * 返回 null 表示这个平台没有自己的取法（或这次没取到），调用方回落到 webview 库的 CookieManager。
 */
expect suspend fun readWebViewCookies(webView: NativeWebView?, url: String): String?

/** 清掉平台网络栈/WebView 里残留的 cookie。 */
internal expect suspend fun clearPlatformCookies()

/**
 * 装 `WebView(onCreated = ...)` 回调给出的原生对象，交给 [readWebViewCookies] 用。
 *
 * 那个回调是 SwingPanel / AndroidView 的 factory 发的，不在组合期，所以用普通持有者
 * 而不是 Compose 状态：它只会被赋一次值，没必要为它引一次重组。
 */
internal class NativeWebViewHolder {
    @kotlin.concurrent.Volatile
    var value: NativeWebView? = null
}

/**
 * cookie 的域可能带前导点表示「含子域」，`hanime1.me` 要能命中 `.hanime1.me`。
 *
 * 站点下发的关键 cookie（登录态、cf_clearance）都是这种带点的域，
 * 按域名精确匹配会把它们整批漏掉——iOS 与桌面读原生 cookie 存储时都要按这个口径过滤。
 */
internal fun String.matchesCookieDomain(domain: String?): Boolean {
    val bare = domain?.trimStart('.')?.lowercase().orEmpty()
    if (bare.isEmpty()) return false
    val host = lowercase()
    return host == bare || host.endsWith(".$bare")
}
