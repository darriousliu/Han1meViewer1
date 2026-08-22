package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

/**
 * 用 ImageIO 的缩略图接口做采样解码：kCGImageSourceThumbnailMaxPixelSize
 * 是系统原生下采样，不会把原图整张解进内存；ThumbnailWithTransform
 * 顺带按 EXIF 转正方向。
 *
 * 末尾 CGImage → JPEG → skia Image 那一段看着绕，但发生在已经缩小之后的图上，
 * 代价可忽略；Compose 侧只认 skia 的 Image，这是最短的一条路。
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun decodeSampledImageBitmap(
    bytes: ByteArray,
    maxDimension: Int,
): ImageBitmap? = withContext(Dispatchers.IO) {
    if (bytes.isEmpty()) return@withContext null

    // ObjC 对象要显式 bridge 成 CF 指针才能喂给 CF 接口，CFBridgingRetain 会 +1，用完各自 release
    val cfData = CFBridgingRetain(bytes.toNSData())
    val source = CGImageSourceCreateWithData(cfData?.reinterpret(), null)
    if (source == null) {
        CFRelease(cfData)
        return@withContext null
    }

    val maxPixelSize = CFBridgingRetain(NSNumber(int = maxDimension))
    val options = CFDictionaryCreateMutable(null, 3, null, null)
    CFDictionarySetValue(options, kCGImageSourceCreateThumbnailFromImageAlways, kCFBooleanTrue)
    CFDictionarySetValue(options, kCGImageSourceCreateThumbnailWithTransform, kCFBooleanTrue)
    CFDictionarySetValue(options, kCGImageSourceThumbnailMaxPixelSize, maxPixelSize)

    val cgImage: CGImageRef? = CGImageSourceCreateThumbnailAtIndex(source, 0u, options)

    CFRelease(options)
    CFRelease(maxPixelSize)
    CFRelease(source)
    CFRelease(cfData)
    if (cgImage == null) return@withContext null

    val jpeg = UIImageJPEGRepresentation(UIImage.imageWithCGImage(cgImage), 1.0)
    CGImageRelease(cgImage)
    if (jpeg == null) return@withContext null
    runCatching { Image.makeFromEncoded(jpeg.toByteArray()).toComposeImageBitmap() }.getOrNull()
}

actual suspend fun encodeJpeg(bitmap: ImageBitmap, quality: Int): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching {
            Image.makeFromBitmap(bitmap.asSkiaBitmap())
                .encodeToData(EncodedImageFormat.JPEG, quality.coerceIn(0, 100))
                ?.bytes
        }.getOrNull()
    }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) {
        result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    }
    return result
}
