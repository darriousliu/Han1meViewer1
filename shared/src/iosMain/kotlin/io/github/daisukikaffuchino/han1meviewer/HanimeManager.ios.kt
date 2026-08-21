package io.github.daisukikaffuchino.han1meviewer

import platform.Foundation.NSHTTPCookieStorage

internal actual suspend fun clearPlatformCookies() {
    val storage = NSHTTPCookieStorage.sharedHTTPCookieStorage
    storage.cookies?.forEach { storage.deleteCookie(it as platform.Foundation.NSHTTPCookie) }
}
