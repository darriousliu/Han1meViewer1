package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import io.ktor.http.Url

/**
 * 外部入口要跳的目标。各平台把自己的入口（Android 的 Intent、iOS 的
 * Universal Links、桌面端的命令行参数）解析成这个，再交给 [navigateTo] 分发。
 */
sealed interface DeepLinkTarget {
    data class Cloudflare(val url: String, val host: String) : DeepLinkTarget
    data class Video(val videoCode: String, val localUri: String? = null) : DeepLinkTarget
    data object DailyCheckIn : DeepLinkTarget
    data class Search(
        val query: String? = null,
        val advancedSearchJson: String? = null,
    ) : DeepLinkTarget
}

fun TopLevelBackStack<HanimeScreen>.navigateTo(target: DeepLinkTarget) {
    when (target) {
        is DeepLinkTarget.Cloudflare ->
            add(CloudflareRoute(url = target.url, host = target.host), launchSingleTop = true)

        is DeepLinkTarget.Video -> add(VideoRoute(target.videoCode, target.localUri))

        DeepLinkTarget.DailyCheckIn -> add(DailyCheckInRoute, launchSingleTop = true)

        is DeepLinkTarget.Search ->
            add(SearchRoute(query = target.query, advancedSearchJson = target.advancedSearchJson))
    }
}

/**
 * 网页链接里带 `v=` 就是一个视频页。认不出来返回 null，交给调用方继续试别的。
 */
fun parseHanimeVideoLink(url: String): DeepLinkTarget.Video? {
    val parsed = runCatching { Url(url) }.getOrNull() ?: return null
    if (parsed.protocol.name != "http" && parsed.protocol.name != "https") return null
    val videoCode = parsed.parameters["v"]?.takeIf { it.isNotBlank() } ?: return null
    return DeepLinkTarget.Video(videoCode)
}

/** 本地视频没有 videoCode，Android 侧一直用的是这个占位值。 */
const val LOCAL_VIDEO_CODE: String = "-1"

/**
 * 应用自己的 scheme。iOS 拿不到 Universal Links（要在 hanime1.com 上放
 * apple-app-site-association，域名不是我们的），只能靠它被外部唤起。
 *
 * `han1meviewer://watch?v=xxxx` / `han1meviewer://checkin` / `han1meviewer://search?q=xxxx`
 */
const val DEEP_LINK_SCHEME: String = "han1meviewer"

/**
 * 从一个 URL 解析出跳转目标。iOS 的 onOpenURL 与桌面的命令行参数共用这条。
 *
 * Android 不走这里：它的入口是 Intent，还带一堆自己的 extra，见 MainIntentHandler。
 */
fun parseDeepLink(url: String): DeepLinkTarget? {
    val parsed = runCatching { Url(url) }.getOrNull() ?: return null
    return when (parsed.protocol.name) {
        "http", "https" -> parseHanimeVideoLink(url)
        "file" -> DeepLinkTarget.Video(LOCAL_VIDEO_CODE, url)
        DEEP_LINK_SCHEME -> parseCustomScheme(parsed)
        else -> null
    }
}

private fun parseCustomScheme(url: Url): DeepLinkTarget? = when (url.host) {
    "watch" -> url.parameters["v"]?.takeIf { it.isNotBlank() }?.let { DeepLinkTarget.Video(it) }
    "checkin" -> DeepLinkTarget.DailyCheckIn
    "search" -> DeepLinkTarget.Search(query = url.parameters["q"]?.takeIf { it.isNotBlank() })
    else -> null
}
