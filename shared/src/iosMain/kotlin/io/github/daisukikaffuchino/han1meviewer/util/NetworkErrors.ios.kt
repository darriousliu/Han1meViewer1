package io.github.daisukikaffuchino.han1meviewer.util

/** TODO(iOS): Darwin engine 的 TLS 失败包在 NSError 里，等接 iOS 网络时再判。 */
internal actual fun Throwable.isSslHandshakeError(): Boolean = false
