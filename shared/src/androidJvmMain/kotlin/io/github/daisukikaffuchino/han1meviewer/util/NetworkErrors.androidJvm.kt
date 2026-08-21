package io.github.daisukikaffuchino.han1meviewer.util

import javax.net.ssl.SSLHandshakeException

internal actual fun Throwable.isSslHandshakeError(): Boolean = this is SSLHandshakeException
