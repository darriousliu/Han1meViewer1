package io.github.daisukikaffuchino.han1meviewer

import android.webkit.CookieManager
import io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar

internal actual suspend fun clearPlatformCookies() {
    HCookieJar.cookieMap.clear()
    CookieManager.getInstance().removeAllCookies(null)
}
