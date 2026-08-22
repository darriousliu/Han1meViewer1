package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import org.jetbrains.compose.resources.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlayerDefaults
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlayerKernel
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.PlayerSettingsScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.PlayerSettingsUiState
import kotlinx.coroutines.launch
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.d_speed_times
import han1meviewer.shared.generated.resources.default_
import han1meviewer.shared.generated.resources.mpv_advanced_settings_summary
import han1meviewer.shared.generated.resources.mpv_settings_disabled_summary
import net.sergeych.sprintf.sprintf
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

@Composable
fun PlayerSettingsRouteScreen(
    onNavigateToMpvSettings: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val settings by SettingsRepository.settings.collectAsStateWithLifecycle()
    val uiState = remember(settings) { buildPlayerSettingsUiState() }

    PlayerSettingsScreen(
        state = uiState,
        kernelOptions = PlayerKernel.entries.map { it.name to it.name },
        speedOptions = PlayerDefaults.speedLabels.zip(PlayerDefaults.speeds.map { it.toString() }),
        longPressSpeedOptions = listOf(
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(1f)) to "1",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(1.5f)) to "1.5",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(2f)) to "2",
            "${stringResource(Res.string.d_speed_times, "%.1f".sprintf(2.5f))} " +
                    "(${stringResource(Res.string.default_)})" to "2.5",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(2.8f)) to "2.8",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(3f)) to "3",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(3.2f)) to "3.2",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(3.5f)) to "3.5",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(3.8f)) to "3.8",
            stringResource(Res.string.d_speed_times, "%.1f".sprintf(4f)) to "4",
        ),
        onKernelChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(playerKernel = io.github.daisukikaffuchino.han1meviewer.logic.model.PlayerKernel.fromValue(it)) } }
        },
        onEnableGoogleCastChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(enableGoogleCast = it) } }
        },
        onShowBottomProgressChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(showBottomProgress = it) } }
        },
        onPlayerSpeedChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(playerSpeed = it.toFloatOrNull() ?: settings.playerSpeed) } }
        },
        onLongPressSpeedChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(longPressSpeedTime = it.toFloatOrNull() ?: settings.longPressSpeedTime) } }
        },
        onSlideSensitivityChange = {
            coroutineScope.launch { SettingsRepository.setSlideSensitivity(it) }
        },
        onOpenMpvSettings = onNavigateToMpvSettings,
    )
}

private fun buildPlayerSettingsUiState(): PlayerSettingsUiState = runBlocking {
    val kernel = SettingsRepository.switchPlayerKernel
    val isMpvPlayer = kernel == PlayerKernel.MpvPlayer.name
    val currentSpeed = SettingsRepository.playerSpeed
    val currentLongPressSpeed = SettingsRepository.longPressSpeedTime
    val speedLabels = PlayerDefaults.speedLabels
    val speedDisplay = speedLabels.getOrElse(
        PlayerDefaults.speeds.indexOfFirst { it == currentSpeed }.takeIf { it >= 0 }
            ?: PlayerDefaults.DEFAULT_SPEED_INDEX
    ) { speedLabels[PlayerDefaults.DEFAULT_SPEED_INDEX] }
    val longPressDisplay =
        getString(Res.string.d_speed_times, "%.1f".sprintf(currentLongPressSpeed))
    PlayerSettingsUiState(
        kernel = kernel,
        kernelDisplay = kernel,
        mpvSettingsEnabled = isMpvPlayer,
        mpvSettingsSummary = if (isMpvPlayer) {
            getString(Res.string.mpv_advanced_settings_summary)
        } else {
            getString(Res.string.mpv_settings_disabled_summary)
        },
        enableGoogleCast = SettingsRepository.enableGoogleCast,
        googleCastAvailable = isGoogleCastAvailable(),
        showBottomProgress = SettingsRepository.showBottomProgress,
        playerSpeed = currentSpeed.toString(),
        playerSpeedLabel = speedDisplay,
        longPressSpeedTimes = currentLongPressSpeed.toString(),
        longPressSpeedTimesLabel = longPressDisplay,
        slideSensitivity = SettingsRepository.slideSensitivity,
        slideSensitivitySummary = toPrettySensitivityString(SettingsRepository.slideSensitivity),
    )
}

/** 只有装了 Google Play 服务的 Android 才有投屏，其余平台隐藏该项。 */
expect fun isGoogleCastAvailable(): Boolean
