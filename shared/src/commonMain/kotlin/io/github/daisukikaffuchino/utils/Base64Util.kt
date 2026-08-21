package io.github.daisukikaffuchino.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Mime 变体会忽略换行等非字母表字符，跟原来 Android Base64 的宽松解码一致。 */
@OptIn(ExperimentalEncodingApi::class)
fun String.decodeFromStringByBase64(): String = Base64.Mime.decode(this).decodeToString()
