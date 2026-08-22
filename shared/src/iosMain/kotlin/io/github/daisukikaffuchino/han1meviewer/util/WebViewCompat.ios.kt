package io.github.daisukikaffuchino.han1meviewer.util

import io.github.kdroidfilter.webview.web.NativeWebView

// WKWebView / wry 默认就开着 DOM storage，不用额外设
actual fun NativeWebView.enableDomStorage() {
}
