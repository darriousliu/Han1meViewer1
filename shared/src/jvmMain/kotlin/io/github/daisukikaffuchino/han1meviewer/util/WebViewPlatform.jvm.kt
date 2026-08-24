package io.github.daisukikaffuchino.han1meviewer.util

import io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar
import io.github.kdroidfilter.webview.web.NativeWebView
import java.net.CookieHandler
import java.net.CookieManager

// WKWebView / wry 默认就开着 DOM storage，不用额外设
actual fun NativeWebView.enableDomStorage() {
}

// 库自带的 CookieManager 在这一端是好用的，不用另外取
actual suspend fun readWebViewCookies(url: String): String? = null

internal actual suspend fun clearPlatformCookies() {
    HCookieJar.cookieMap.clear()
    (CookieHandler.getDefault() as? CookieManager)?.cookieStore?.removeAll()
}
