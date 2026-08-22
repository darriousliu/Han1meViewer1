package io.github.daisukikaffuchino.han1meviewer.util

/** getchu 站点是 EUC-JP 编码，各平台的解码方式不一样。 */
expect fun ByteArray.decodeEucJp(): String
