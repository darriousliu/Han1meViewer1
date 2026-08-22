package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

/**
 * JDK 自带的 ImageReadParam.setSourceSubsampling 就是真子采样读取：
 * reader.getWidth/getHeight 只读文件头不分配像素，算完采样率再 read。
 */
actual suspend fun decodeSampledImageBitmap(
    bytes: ByteArray,
    maxDimension: Int,
): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        ImageIO.createImageInputStream(ByteArrayInputStream(bytes)).use { input ->
            val reader = ImageIO.getImageReaders(input).asSequence().firstOrNull()
                ?: return@use null
            try {
                reader.setInput(input, true, true)
                val sample =
                    computeSampleSize(reader.getWidth(0), reader.getHeight(0), maxDimension)
                val param = reader.defaultReadParam.apply {
                    setSourceSubsampling(sample, sample, 0, 0)
                }
                reader.read(0, param)?.toComposeImageBitmap()
            } finally {
                reader.dispose()
            }
        }
    }.getOrNull()
}

actual suspend fun encodeJpeg(bitmap: ImageBitmap, quality: Int): ByteArray? =
    withContext(Dispatchers.IO) {
        runCatching {
            // JPEG 没有 alpha，带 alpha 的 BufferedImage 直接写会得到全黑图，
            // 先重绘到 TYPE_INT_RGB
            val source = bitmap.toAwtImage()
            val opaque = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_RGB)
            opaque.createGraphics().run {
                drawImage(source, 0, 0, null)
                dispose()
            }
            // ImageIO.write 配不了画质，得自己拿 writer
            val writer = ImageIO.getImageWritersByFormatName("jpg").asSequence().firstOrNull()
                ?: return@runCatching null
            val param = writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = quality.coerceIn(0, 100) / 100f
            }
            ByteArrayOutputStream().use { out ->
                ImageIO.createImageOutputStream(out).use { imageOut ->
                    writer.output = imageOut
                    try {
                        writer.write(null, IIOImage(opaque, null, null), param)
                    } finally {
                        writer.dispose()
                    }
                }
                out.toByteArray()
            }
        }.getOrNull()
    }
