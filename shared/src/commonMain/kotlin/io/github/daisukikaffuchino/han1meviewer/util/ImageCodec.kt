package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 图片的采样解码与编码。存在的唯一理由是别让一张大图把内存打爆：
 * 8000×6000 的相机原片整张解成 ARGB_8888 就是 183 MB。
 *
 * 现成的路子（FileKit 的压缩、CMP 的 decodeToImageBitmap）都是先全量解码再缩放，
 * 采样必须发生在解码器内部，只能各平台各写一份。
 */

/** 预览解码的尺寸上限。2048² 的 ARGB_8888 最坏 16 MB，取景够用。 */
const val PREVIEW_MAX_DIMENSION = 2048

/** 头像输出的尺寸上限。 */
const val AVATAR_MAX_DIMENSION = 1024

/** 头像 JPEG 画质。 */
const val AVATAR_JPEG_QUALITY = 90

/**
 * 算出让 `max(宽, 高) / 采样率 <= [maxDimension]` 成立的最小 2 的幂次。
 * 各平台解码器都按整数倍抽行抽列，取 2 的幂次能让抽样落在字节边界上、行为一致。
 * 尺寸非法（读不到图片头时会这样）返回 1，交给调用方按解码失败处理。
 */
fun computeSampleSize(srcWidth: Int, srcHeight: Int, maxDimension: Int): Int {
    if (srcWidth <= 0 || srcHeight <= 0 || maxDimension <= 0) return 1
    val longest = maxOf(srcWidth, srcHeight)
    var sample = 1
    while (longest / sample > maxDimension) sample = sample shl 1
    return sample
}

/**
 * 采样解码：先只读图片头拿原始尺寸（不分配像素），算出采样率再解，
 * 保证结果最长边不超过 [maxDimension]。解不出来返回 null，不抛。
 * 各平台都会顺带按 EXIF 把方向转正。
 */
expect suspend fun decodeSampledImageBitmap(bytes: ByteArray, maxDimension: Int): ImageBitmap?

/** 编码成 JPEG 字节，失败返回 null。 */
expect suspend fun encodeJpeg(bitmap: ImageBitmap, quality: Int): ByteArray?
