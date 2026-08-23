package io.github.daisukikaffuchino.han1meviewer.util

/**
 * 直接从平台的 WebView cookie 存储里取该域下的 cookie，拼成 `a=1; b=2`。
 *
 * 返回 null 表示这个平台没有自己的取法，调用方回落到 webview 库的 CookieManager。
 */
expect suspend fun readWebViewCookies(url: String): String?
