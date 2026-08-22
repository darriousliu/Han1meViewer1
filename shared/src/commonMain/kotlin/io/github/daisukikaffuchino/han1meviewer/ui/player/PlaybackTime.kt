package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.daisukikaffuchino.han1meviewer.util.pad2

fun formatPlaybackTime(positionMs: Long): String {
    val totalSeconds = (positionMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "$hours:${minutes.toInt().pad2()}:${seconds.toInt().pad2()}"
    } else {
        "${minutes.toInt().pad2()}:${seconds.toInt().pad2()}"
    }
}
