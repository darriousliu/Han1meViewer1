package io.github.daisukikaffuchino.han1meviewer.util

/**
 * 签名校验。Android 上是 libchino 里的 JNI 实现，其余平台没有壳可校验，直接放行。
 */
internal expect fun signatureCheckResult(): String

internal expect fun isSignatureValid(): Boolean
