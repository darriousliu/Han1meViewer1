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
 * 返回 null 表示这个平台没有自己的取法，调用方回落到 webview 库的 CookieManager。
 */
expect suspend fun readWebViewCookies(url: String): String?

/** 清掉平台网络栈/WebView 里残留的 cookie。 */
internal expect suspend fun clearPlatformCookies()
