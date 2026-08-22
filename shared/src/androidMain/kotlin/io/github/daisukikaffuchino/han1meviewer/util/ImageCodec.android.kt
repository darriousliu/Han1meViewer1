package io.github.daisukikaffuchino.han1meviewer.util

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * minSdk 29，直接走 ImageDecoder：setTargetSampleSize 是解码时下采样，
 * 而且它按 EXIF 自动把方向转正。
 */
actual suspend fun decodeSampledImageBitmap(
    bytes: ByteArray,
    maxDimension: Int,
): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.setTargetSampleSize(
                computeSampleSize(info.size.width, info.size.height, maxDimension)
            )
            // 默认可能给 HARDWARE bitmap，那种读不到像素，后面裁剪要读
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }
    }.getOrNull()?.asImageBitmap()
}

actual suspend fun encodeJpeg(bitmap: ImageBitmap, quality: Int): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching {
            ByteArrayOutputStream().use { out ->
                bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.toByteArray()
            }
        }.getOrNull()
    }
