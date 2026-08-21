package io.github.daisukikaffuchino.han1meviewer.util

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.home_error_connect
import han1meviewer.shared.generated.resources.home_error_connection_interrupted
import han1meviewer.shared.generated.resources.home_error_connection_reset
import han1meviewer.shared.generated.resources.home_error_dns
import han1meviewer.shared.generated.resources.home_error_forbidden
import han1meviewer.shared.generated.resources.home_error_generic
import han1meviewer.shared.generated.resources.home_error_not_found
import han1meviewer.shared.generated.resources.home_error_server_unavailable
import han1meviewer.shared.generated.resources.home_error_ssl
import han1meviewer.shared.generated.resources.home_error_timeout
import org.jetbrains.compose.resources.StringResource

/** 这几个判定都依赖平台异常类型，Darwin 与 JVM 不是一回事。 */
internal expect fun Throwable.isSslHandshakeError(): Boolean
internal expect fun Throwable.isDnsError(): Boolean
internal expect fun Throwable.isTimeoutError(): Boolean
internal expect fun Throwable.isConnectError(): Boolean
internal expect fun Throwable.isConnectionResetError(): Boolean

/**
 * 将加载异常映射为对应的错误提示字符串资源。
 *
 * 优先根据异常类型判断常见网络问题，必要时回退到异常信息中的关键字匹配。
 */
fun Throwable.toNetworkErrorMessageRes(): StringResource {
    val rawMessage = message.orEmpty().lowercase()
    return when {
        isDnsError() ||
                rawMessage.contains("unable to resolve host") ||
                rawMessage.contains("no address associated with hostname") -> Res.string.home_error_dns

        isTimeoutError() || rawMessage.contains("timeout") -> Res.string.home_error_timeout

        isSslHandshakeError() ||
                rawMessage.contains("ssl") ||
                rawMessage.contains("certificate") -> Res.string.home_error_ssl

        isConnectError() || rawMessage.contains("failed to connect") -> Res.string.home_error_connect

        isConnectionResetError() &&
                rawMessage.contains("connection reset") -> Res.string.home_error_connection_interrupted

        rawMessage.contains("connection reset") -> Res.string.home_error_connection_reset

        rawMessage.contains("403") -> Res.string.home_error_forbidden
        rawMessage.contains("404") -> Res.string.home_error_not_found
        rawMessage.contains("500") || rawMessage.contains("502") ||
                rawMessage.contains("503") || rawMessage.contains("504") ->
            Res.string.home_error_server_unavailable

        else -> Res.string.home_error_generic
    }
}
