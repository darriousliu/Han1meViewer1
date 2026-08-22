package io.github.daisukikaffuchino.utils

import net.sergeych.sprintf.sprintf


private val SI_UNITS = arrayOf("B", "K", "M", "G", "T")
private val IEC_UNITS = arrayOf("B", "KiB", "MiB", "GiB", "TiB")

fun Long.formatFileSize(
    useSi: Boolean = true,
    decimalPlaces: Int = 1,
    stripTrailingZeros: Boolean = true,
): String {
    val unit = if (useSi) 1000 else 1024
    if (this < unit) return "$this B"

    val units = if (useSi) SI_UNITS else IEC_UNITS
    var value = toDouble()
    var unitIndex = 0

    while (value >= unit && unitIndex < units.size - 1) {
        value /= unit
        unitIndex++
    }

    return if (decimalPlaces == 0 || (stripTrailingZeros && value % 1 == 0.0)) {
        "%.0f %s".sprintf(value, units[unitIndex])
    } else {
        "%.${decimalPlaces}f %s".sprintf(value, units[unitIndex])
    }
}

fun Long.formatBytesPerSecond(
    useSi: Boolean = true,
    decimalPlaces: Int = 1,
    stripTrailingZeros: Boolean = true,
): String {
    return formatFileSize(useSi, decimalPlaces, stripTrailingZeros) + "/s"
}
