package io.github.daisukikaffuchino.han1meviewer.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSJapaneseEUCStringEncoding
import platform.Foundation.NSString
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ByteArray.decodeEucJp(): String {
    if (isEmpty()) return ""
    return usePinned { pinned ->
        NSString.create(
            bytes = pinned.addressOf(0),
            length = size.toULong(),
            encoding = NSJapaneseEUCStringEncoding,
        )?.toString()
    }.orEmpty()
}
