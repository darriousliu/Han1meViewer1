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
