package io.github.daisukikaffuchino.han1meviewer.util

import io.github.kdroidfilter.webview.web.NativeWebView

/**
 * Android 的 WebView 默认不开 DOM storage，而 Cloudflare 过盾页要用 localStorage。
 * 这一版 webview 库的 AndroidWebSettings 没暴露这个开关，只能从 onCreated 拿原生对象设。
 */
expect fun NativeWebView.enableDomStorage()
