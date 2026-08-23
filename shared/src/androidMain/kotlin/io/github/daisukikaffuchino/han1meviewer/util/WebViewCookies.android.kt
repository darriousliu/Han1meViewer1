package io.github.daisukikaffuchino.han1meviewer.util

// 库自带的 CookieManager 在这一端是好用的，不用另外取
actual suspend fun readWebViewCookies(url: String): String? = null
