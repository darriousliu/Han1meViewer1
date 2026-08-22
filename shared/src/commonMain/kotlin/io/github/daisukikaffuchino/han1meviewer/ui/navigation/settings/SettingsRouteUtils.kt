@file:Suppress("DEPRECATION")

package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import androidx.annotation.IntRange
import be.digitalia.compose.htmlconverter.htmlToString
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants.HANIME_HOSTNAME
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants.HANIME_URL
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlayerDefaults
import io.github.daisukikaffuchino.utils.formatBytesPerSecond
import io.github.daisukikaffuchino.utils.formatFileSize
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.alternative
import han1meviewer.shared.generated.resources.cache_usage_summary
import han1meviewer.shared.generated.resources.current_slide_sensitivity
import han1meviewer.shared.generated.resources.default_
import han1meviewer.shared.generated.resources.extremely_high
import han1meviewer.shared.generated.resources.extremely_low
import han1meviewer.shared.generated.resources.high
import han1meviewer.shared.generated.resources.low
import han1meviewer.shared.generated.resources.moderate
import han1meviewer.shared.generated.resources.no_limit
import han1meviewer.shared.generated.resources.slightly_high
import han1meviewer.shared.generated.resources.slightly_low
import han1meviewer.shared.generated.resources.will_remind_before_d_seconds
import org.jetbrains.compose.resources.getString

internal suspend fun buildDomainOptions(): List<Pair<String, String>> = listOf(
    "${HANIME_HOSTNAME[0]} (${getString(Res.string.default_)})" to HANIME_URL[0],
    "${HANIME_HOSTNAME[1]} (${getString(Res.string.alternative)})" to HANIME_URL[1],
    "${HANIME_HOSTNAME[2]} (${getString(Res.string.alternative)})" to HANIME_URL[2],
    "${HANIME_HOSTNAME[3]} (av)" to HANIME_URL[3],
)

internal suspend fun generateClearCacheSummary(size: Long): CharSequence {
    return htmlToString(getString(Res.string.cache_usage_summary, size.formatFileSize()))
}

internal suspend fun toPrettySensitivityString(
    @IntRange(from = 1, to = 7) value: Int
): String {
    val pretty = when (value) {
        1 -> getString(Res.string.extremely_low)
        2 -> getString(Res.string.low)
        3 -> getString(Res.string.slightly_low)
        4 -> getString(Res.string.moderate)
        5 -> getString(Res.string.slightly_high)
        6 -> getString(Res.string.high)
        7 -> getString(Res.string.extremely_high)
        else -> error("Invalid sensitivity value: $value")
    }
    return getString(Res.string.current_slide_sensitivity, pretty)
}

internal suspend fun toPrettyCountdownRemindString(
    @IntRange(from = 5, to = 30) value: Int
): String {
    return buildString {
        append(getString(Res.string.will_remind_before_d_seconds, value))
        if (value == PlayerDefaults.DEFAULT_COUNTDOWN_SECONDS) {
            append(" (${getString(Res.string.default_)})")
        }
    }
}

internal suspend fun Long.toDownloadSpeedPrettyString(): String {
    return if (this == 0L) {
        getString(Res.string.no_limit)
    } else {
        formatBytesPerSecond()
    }
}

internal suspend fun toDownloadCountLimitPrettyString(value: Int): String {
    return if (value == 0) getString(Res.string.no_limit) else value.toString()
}

internal expect fun isDeviceSecureCompat(): Boolean

internal expect fun isPipPermissionGranted(): Boolean

internal expect fun openPipPermissionSettings()
