package io.github.daisukikaffuchino.han1meviewer.util

import android.webkit.CookieManager
import io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar
import io.github.kdroidfilter.webview.web.NativeWebView

actual fun NativeWebView.enableDomStorage() {
    settings.domStorageEnabled = true
}

// 库自带的 CookieManager 在这一端是好用的，不用另外取
actual suspend fun readWebViewCookies(url: String): String? = null

internal actual suspend fun clearPlatformCookies() {
    HCookieJar.cookieMap.clear()
    CookieManager.getInstance().removeAllCookies(null)
}
