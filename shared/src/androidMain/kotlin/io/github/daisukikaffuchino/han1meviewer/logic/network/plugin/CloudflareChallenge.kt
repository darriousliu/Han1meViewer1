package io.github.daisukikaffuchino.han1meviewer.logic.network.plugin

import io.github.daisukikaffuchino.han1meviewer.logic.network.CloudflareVerificationCoordinator
import io.github.daisukikaffuchino.utils.applicationContext
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.plugin
import java.io.IOException

/** 撞到 cf 盾就拉起 WebView 过盾，过完原样重发一次 */
val CloudflareChallenge = createClientPlugin("CloudflareChallenge") {
    client.plugin(HttpSend).intercept { request ->
        val call = execute(request)
        val response = call.response
        if (response.status.value == 403 && response.headers["cf-mitigated"] == "challenge") {
            val verified = CloudflareVerificationCoordinator.verify(
                context = applicationContext,
                url = request.url.buildString(),
            )
            if (!verified) {
                throw IOException("Cloudflare verification was cancelled, failed, or timed out")
            }
            execute(request)
        } else {
            call
        }
    }
}
