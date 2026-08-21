package io.github.daisukikaffuchino.han1meviewer

import io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar

// 桌面端没有 WebView，cookie 只存在 HCookieJar 里
internal actual suspend fun clearPlatformCookies() {
    HCookieJar.cookieMap.clear()
}
