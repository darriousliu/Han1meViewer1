package io.github.daisukikaffuchino.han1meviewer.util

// ios 上没有 apk 壳可校验
internal actual fun signatureCheckResult(): String = ""

internal actual fun isSignatureValid(): Boolean = true
