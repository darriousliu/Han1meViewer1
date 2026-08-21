package io.github.daisukikaffuchino.han1meviewer.util

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import platform.Foundation.NSURLErrorClientCertificateRejected
import platform.Foundation.NSURLErrorClientCertificateRequired
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorTimedOut
import platform.Foundation.NSURLErrorSecureConnectionFailed
import platform.Foundation.NSURLErrorServerCertificateHasBadDate
import platform.Foundation.NSURLErrorServerCertificateHasUnknownRoot
import platform.Foundation.NSURLErrorServerCertificateNotYetValid
import platform.Foundation.NSURLErrorServerCertificateUntrusted

private fun Throwable.matchesUrlErrorCode(codes: Set<Long>): Boolean {
    var current: Throwable? = this
    while (current != null) {
        val origin = (current as? DarwinHttpRequestException)?.origin
        if (origin != null && origin.domain == NSURLErrorDomain && origin.code in codes) return true
        val next = current.cause
        current = if (next === current) null else next
    }
    return false
}

internal actual fun Throwable.isDnsError(): Boolean =
    matchesUrlErrorCode(setOf(NSURLErrorCannotFindHost, NSURLErrorDNSLookupFailed))

internal actual fun Throwable.isTimeoutError(): Boolean =
    matchesUrlErrorCode(setOf(NSURLErrorTimedOut))

internal actual fun Throwable.isConnectError(): Boolean =
    matchesUrlErrorCode(setOf(NSURLErrorCannotConnectToHost))

internal actual fun Throwable.isConnectionResetError(): Boolean =
    matchesUrlErrorCode(setOf(NSURLErrorNetworkConnectionLost))

private val tlsErrorCodes = setOf(
    NSURLErrorSecureConnectionFailed,
    NSURLErrorServerCertificateHasBadDate,
    NSURLErrorServerCertificateUntrusted,
    NSURLErrorServerCertificateHasUnknownRoot,
    NSURLErrorServerCertificateNotYetValid,
    NSURLErrorClientCertificateRejected,
    NSURLErrorClientCertificateRequired,
)

internal actual fun Throwable.isSslHandshakeError(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        val origin = (current as? DarwinHttpRequestException)?.origin
        if (origin != null && origin.domain == NSURLErrorDomain && origin.code in tlsErrorCodes) {
            return true
        }
        val next = current.cause
        current = if (next === current) null else next
    }
    return false
}
