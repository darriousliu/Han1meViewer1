package io.github.daisukikaffuchino.han1meviewer.logic.network.plugin

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders

/**
 * 把登录 cookie 附到请求上。
 *
 * Android 与桌面走 OkHttp 的 [io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar]，
 * iOS 用的是 Darwin 引擎、没有 CookieJar 这一层，登录 cookie 从来没被带上去，
 * 结果就是登录成功、首页却仍是未登录态，被判成登录失效又自动登出。
 *
 * 每次请求现取，不缓存：登录/登出只改设置，不会重建 client。
 */
val AttachStoredCookies = createClientPlugin("AttachStoredCookies") {
    onRequest { request, _ ->
        val host = request.url.host
        val cookies = buildList {
            SettingsRepository.current.loginCookie.trim()
                .takeIf { it.isNotEmpty() }?.let(::add)
            if (SettingsRepository.cloudFlareCookieHost == host) {
                SettingsRepository.current.cloudFlareCookie.trim()
                    .takeIf { it.isNotEmpty() }?.let(::add)
            }
        }
        if (cookies.isEmpty()) return@onRequest
        // 与引擎自己存的 cookie 合并到同一个头里，避免发出两个 Cookie 头
        val merged = (listOfNotNull(request.headers[HttpHeaders.Cookie]) + cookies)
            .filter { it.isNotBlank() }
            .joinToString("; ")
        request.headers[HttpHeaders.Cookie] = merged
    }
}
