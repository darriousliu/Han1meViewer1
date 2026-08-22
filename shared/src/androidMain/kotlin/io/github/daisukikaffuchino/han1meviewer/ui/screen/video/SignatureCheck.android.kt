package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

internal actual fun signatureCheckResult(): String = getString()

internal actual fun isSignatureValid(): Boolean = svc()

private external fun svc(): Boolean

private external fun getString(): String
