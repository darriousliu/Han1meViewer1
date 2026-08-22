// ⚠️ JNI 按「包名 + JVM facade 类名」解析符号，改文件名/包名/@JvmName 都会让
// libchino 里的 Java_... 符号对不上，表现是运行时 UnsatisfiedLinkError、播放页进不去。
// 对应实现在 app/src/main/cpp/chino.cpp，两边必须一起改。
@file:JvmName("SignatureCheckKt")

package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

internal actual fun signatureCheckResult(): String = getString()

internal actual fun isSignatureValid(): Boolean = svc()

private external fun svc(): Boolean

private external fun getString(): String
