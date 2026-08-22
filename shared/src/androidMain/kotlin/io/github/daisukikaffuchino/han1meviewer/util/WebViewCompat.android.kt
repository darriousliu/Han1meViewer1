package io.github.daisukikaffuchino.han1meviewer.util

import io.github.kdroidfilter.webview.web.NativeWebView

actual fun NativeWebView.enableDomStorage() {
    settings.domStorageEnabled = true
}
