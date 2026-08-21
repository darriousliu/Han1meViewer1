package io.github.daisukikaffuchino.han1meviewer.util

import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

internal actual fun Throwable.isSslHandshakeError(): Boolean = this is SSLHandshakeException
internal actual fun Throwable.isDnsError(): Boolean = this is UnknownHostException
internal actual fun Throwable.isTimeoutError(): Boolean = this is SocketTimeoutException
internal actual fun Throwable.isConnectError(): Boolean = this is ConnectException
internal actual fun Throwable.isConnectionResetError(): Boolean = this is SocketException
