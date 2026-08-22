package io.github.daisukikaffuchino.han1meviewer.util

import java.nio.charset.Charset

private val EUC_JP: Charset = Charset.forName("EUC-JP")

actual fun ByteArray.decodeEucJp(): String = toString(EUC_JP)
