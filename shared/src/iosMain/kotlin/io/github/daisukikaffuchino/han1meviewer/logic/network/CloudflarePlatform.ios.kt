package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.ktor.client.HttpClientConfig

/** TODO: 过盾流程需要一个可嵌 WebView 的宿主，本平台还没有。 */
internal actual fun HttpClientConfig<*>.installCloudflareChallenge() = Unit
