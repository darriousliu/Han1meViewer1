package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.github.daisukikaffuchino.han1meviewer.logic.network.plugin.CloudflareChallenge
import io.ktor.client.HttpClientConfig

internal actual fun HttpClientConfig<*>.installCloudflareChallenge() {
    install(CloudflareChallenge)
}
