package io.github.daisukikaffuchino.han1meviewer

import io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar
import java.net.CookieHandler
import java.net.CookieManager

internal actual suspend fun clearPlatformCookies() {
    HCookieJar.cookieMap.clear()
    (CookieHandler.getDefault() as? CookieManager)?.cookieStore?.removeAll()
}
