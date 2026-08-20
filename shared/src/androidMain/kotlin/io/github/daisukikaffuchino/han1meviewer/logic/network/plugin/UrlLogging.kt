package io.github.daisukikaffuchino.han1meviewer.logic.network.plugin

import io.github.daisukikaffuchino.utils.LogUtil
import io.ktor.client.plugins.api.createClientPlugin
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** 只打 URL，不用 Ktor 自带的 Logging：那个在 HEADERS 级别会把会话 cookie 也打出来 */
val UrlLogging = createClientPlugin("UrlLogging") {
    onRequest { request, _ ->
        val url = request.url.buildString()
        LogUtil.i("NetworkRequest", URLDecoder.decode(url, StandardCharsets.UTF_8.name()))
    }
}
