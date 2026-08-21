package io.github.daisukikaffuchino.han1meviewer.util

/** SSL 握手失败的判定依赖平台异常类型，Darwin 与 JVM 不是一回事。 */
internal expect fun Throwable.isSslHandshakeError(): Boolean
