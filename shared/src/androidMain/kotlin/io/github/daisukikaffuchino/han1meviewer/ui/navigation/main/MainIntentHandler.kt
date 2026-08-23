package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import android.content.Intent
import kotlinx.serialization.json.Json

const val EXTRA_OPEN_DAILY_CHECK_IN = "openDailyCheckIn"
const val ACTION_OPEN_CLOUDFLARE_VERIFICATION =
    "io.github.daisukikaffuchino.han1meviewer.action.OPEN_CLOUDFLARE_VERIFICATION"
const val EXTRA_CLOUDFLARE_URL = "cloudflare_url"
const val EXTRA_CLOUDFLARE_HOST = "cloudflare_host"

/**
 * Intent 解析成 [DeepLinkTarget]。命中的 extra 会就地清掉、action 置空，
 * 免得 Activity 重建后又跳一次。
 */
internal fun Intent.consumeDeepLinkTarget(): DeepLinkTarget? {
    if (action == ACTION_OPEN_CLOUDFLARE_VERIFICATION) {
        val url = getStringExtra(EXTRA_CLOUDFLARE_URL)
        val host = getStringExtra(EXTRA_CLOUDFLARE_HOST)
        removeExtra(EXTRA_CLOUDFLARE_URL)
        removeExtra(EXTRA_CLOUDFLARE_HOST)
        action = null
        if (url.isNullOrBlank() || host.isNullOrBlank()) return null
        return DeepLinkTarget.Cloudflare(url = url, host = host)
    }

    data?.let { uri ->
        when (uri.scheme) {
            // 网页链接的解析是公共的，iOS 的 Universal Links 也走同一条
            "http", "https" -> parseHanimeVideoLink(uri.toString())?.let { return it }
            // content:// 是 Android 独有的，file:// 也按本地播放处理
            "file", "content" -> return DeepLinkTarget.Video("-1", uri.toString())
        }
    }

    if (getBooleanExtra(EXTRA_OPEN_DAILY_CHECK_IN, false)) {
        removeExtra(EXTRA_OPEN_DAILY_CHECK_IN)
        return DeepLinkTarget.DailyCheckIn
    }

    getStringExtra("startSearchFromTag")?.let { tag ->
        removeExtra("startSearchFromTag")
        return DeepLinkTarget.Search(query = tag)
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    val map = getSerializableExtra("startSearchFromMap") as? HashMap<String, String>
    if (map != null) {
        removeExtra("startSearchFromMap")
        return DeepLinkTarget.Search(advancedSearchJson = Json.encodeToString(map))
    }

    val videoCode = getStringExtra("startVideoCode")
    if (!videoCode.isNullOrEmpty()) {
        removeExtra("startVideoCode")
        return DeepLinkTarget.Video(videoCode)
    }
    return null
}
