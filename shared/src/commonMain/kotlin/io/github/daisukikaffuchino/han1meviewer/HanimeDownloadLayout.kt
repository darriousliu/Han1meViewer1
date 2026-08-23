package io.github.daisukikaffuchino.han1meviewer

/**
 * 下载目录的布局约定，三端共用：
 *
 * ```
 * <下载根目录>/hanime_download/<videoCode>/
 *     ├── <标题>_<画质>.mp4
 *     ├── <标题>.png
 *     └── info.json
 * ```
 */
object HanimeDownloadLayout {

    const val HANIME_DOWNLOAD_FOLDER = "hanime_download"
    const val VIDEO_INFO_FILE = "info.json"
    const val DEF_VIDEO_COVER_TYPE = "png"

    val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "flv", "mov", "webm")
    val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

    fun createVideoName(title: String, quality: String, suffix: String): String =
        "${title.replaceIllegalChars()}_${quality}.$suffix"

    fun createVideoCoverName(title: String, suffix: String): String =
        "${title.replaceIllegalChars()}.$suffix"

    // 不用正则：控制字符要写成 \xNN，各平台 Regex 引擎对它的支持没保证
    private const val ILLEGAL_FILENAME_CHARS = "\"*/:<>?\\|"

    private fun String.replaceIllegalChars(): String = map { ch ->
        if (ch in ILLEGAL_FILENAME_CHARS || ch.code < 0x20 || ch.code == 0x7F) '_' else ch
    }.joinToString("")
}
