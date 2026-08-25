package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import platform.Foundation.NSURL

/**
 * iosApp 的 `ContentView.swift` 在 onOpenURL 里调这里。认不出来的 URL 直接丢掉。
 */
object IosDeepLink {

    fun handle(url: String) {
        val target = parseDeepLink(url) ?: return
        val localUri = (target as? DeepLinkTarget.Video)?.localUri
        if (localUri != null) {
            // 「文件」App 里就地打开的文件在沙盒外，不开安全作用域读不了。
            // 刻意不配对 stop：播放器还要接着读，作用域得一直开着，应用退出时系统自己收回。
            NSURL.URLWithString(localUri)?.startAccessingSecurityScopedResource()
        }
        DeepLinkBus.post(target)
    }
}
