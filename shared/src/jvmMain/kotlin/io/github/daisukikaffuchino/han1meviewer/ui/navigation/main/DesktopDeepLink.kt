package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import io.github.daisukikaffuchino.utils.LogUtil
import java.awt.Desktop
import java.io.File

/**
 * 桌面端的外部入口：命令行参数，以及 macOS 的「打开方式」。
 *
 * macOS 不把这些放进 argv，而是发 AppleEvent，得单独挂 handler；应用已经开着时
 * 再打开一个链接也只会走 handler 这条。
 */
fun postDeepLinkFromArguments(args: Array<String>) {
    args.firstNotNullOfOrNull(::parseLaunchArgument)?.let(DeepLinkBus::post)
}

fun installSystemDeepLinkHandlers() {
    val desktop = runCatching { Desktop.getDesktop() }.getOrNull() ?: return
    runCatching {
        if (desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
            desktop.setOpenURIHandler { event ->
                parseDeepLink(event.uri.toString())?.let(DeepLinkBus::post)
            }
        }
        if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
            desktop.setOpenFileHandler { event ->
                event.files.firstOrNull()
                    ?.let { DeepLinkTarget.Video(LOCAL_VIDEO_CODE, it.toURI().toString()) }
                    ?.let(DeepLinkBus::post)
            }
        }
    }.onFailure { LogUtil.w("DeepLink", "挂系统入口失败", it) }
}

/** 参数可能是 URL，也可能是拖上来的裸路径。 */
private fun parseLaunchArgument(arg: String): DeepLinkTarget? {
    parseDeepLink(arg)?.let { return it }
    val file = File(arg)
    if (!file.isFile) return null
    return DeepLinkTarget.Video(LOCAL_VIDEO_CODE, file.toURI().toString())
}
