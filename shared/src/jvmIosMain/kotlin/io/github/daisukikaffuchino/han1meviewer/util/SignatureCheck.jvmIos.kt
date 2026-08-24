package io.github.daisukikaffuchino.han1meviewer.util

// 非 Android 平台没有 apk 壳可校验，直接放行
internal actual fun signatureCheckResult(): String = ""

internal actual fun isSignatureValid(): Boolean = true
