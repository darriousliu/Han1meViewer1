package io.github.daisukikaffuchino.han1meviewer.logic.network

import io.ktor.client.HttpClientConfig

/** 过盾要弹 WebView，只有 Android 有；其它平台是空实现。 */
internal expect fun HttpClientConfig<*>.installCloudflareChallenge()
