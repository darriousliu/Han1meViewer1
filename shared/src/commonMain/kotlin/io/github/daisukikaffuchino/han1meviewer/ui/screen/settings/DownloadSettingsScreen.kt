package io.github.daisukikaffuchino.han1meviewer.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.daisukikaffuchino.han1meviewer.ui.component.SettingNavigationItem
import io.github.daisukikaffuchino.han1meviewer.ui.component.SettingSliderItem
import io.github.daisukikaffuchino.han1meviewer.ui.component.SettingsSectionTitle
import io.github.daisukikaffuchino.han1meviewer.ui.component.SettingsSegmentedGroup
import io.github.daisukikaffuchino.han1meviewer.ui.component.lazy.LazyColumn
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.isDownloadSpeedLimitSupported
import io.github.daisukikaffuchino.han1meviewer.ui.preview.ComponentPreview
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.download
import han1meviewer.shared.generated.resources.download_count_limit
import han1meviewer.shared.generated.resources.download_path
import han1meviewer.shared.generated.resources.download_speed_limit
import han1meviewer.shared.generated.resources.pref_export_downloads_summary
import han1meviewer.shared.generated.resources.pref_export_downloads_title
import han1meviewer.shared.generated.resources.ic_count
import han1meviewer.shared.generated.resources.ic_export
import han1meviewer.shared.generated.resources.ic_file_path
import han1meviewer.shared.generated.resources.ic_speed

data class DownloadSettingsUiState(
    val downloadPathSummary: String,
    val downloadCountLimit: Int,
    val downloadCountLimitSummary: String,
    val downloadSpeedLimitIndex: Int,
    val downloadSpeedLimitSummary: String,
)

@Composable
fun DownloadSettingsScreen(
    state: DownloadSettingsUiState,
    maxDownloadCountLimit: Int,
    maxDownloadSpeedLimitIndex: Int,
    onOpenDownloadPath: () -> Unit,
    onRestoreDefaultPath: () -> Unit,
    onImportDownloadedFiles: (() -> Unit)?,
    onDownloadCountLimitChange: (Int) -> Unit,
    onDownloadSpeedLimitChange: (Int) -> Unit,
    embedded: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Column {
            if (embedded) {
                SettingsSectionTitle(titleRes = Res.string.download)
            }
            SettingsSegmentedGroup {
                SettingNavigationItem(
                    title = stringResource(Res.string.download_path),
                    summary = state.downloadPathSummary,
                    iconRes = Res.drawable.ic_file_path,
                    onClick = onOpenDownloadPath,
                )
                if (onImportDownloadedFiles != null) {
                    SettingNavigationItem(
                        title = stringResource(Res.string.pref_export_downloads_title),
                        summary = stringResource(Res.string.pref_export_downloads_summary),
                        iconRes = Res.drawable.ic_export,
                        onClick = onImportDownloadedFiles,
                    )
                }
                SettingSliderItem(
                    title = stringResource(Res.string.download_count_limit),
                    summary = state.downloadCountLimitSummary,
                    value = state.downloadCountLimit,
                    valueRange = 0..maxDownloadCountLimit,
                    iconRes = Res.drawable.ic_count,
                    onValueChange = onDownloadCountLimitChange,
                )
                if (isDownloadSpeedLimitSupported) {
                    SettingSliderItem(
                        title = stringResource(Res.string.download_speed_limit),
                        summary = state.downloadSpeedLimitSummary,
                        value = state.downloadSpeedLimitIndex,
                        valueRange = 0..maxDownloadSpeedLimitIndex,
                        iconRes = Res.drawable.ic_speed,
                        onValueChange = onDownloadSpeedLimitChange,
                    )
                }
            }
        }
    }
    if (embedded) {
        content()
    } else {
        LazyColumn(
            enableItemAnimation = false,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            item { content() }
        }
    }
}

@Preview
@Composable
private fun DownloadSettingsScreenPreview() {
    ComponentPreview {
        DownloadSettingsScreen(
            state = DownloadSettingsUiState(
                downloadPathSummary = "/storage/emulated/0/Android/data/.../files",
                downloadCountLimit = 2,
                downloadCountLimitSummary = "2",
                downloadSpeedLimitIndex = 0,
                downloadSpeedLimitSummary = "无限制",
            ),
            maxDownloadCountLimit = 10,
            maxDownloadSpeedLimitIndex = 5,
            onOpenDownloadPath = {},
            onRestoreDefaultPath = {},
            onImportDownloadedFiles = {},
            onDownloadCountLimitChange = {},
            onDownloadSpeedLimitChange = {},
        )
    }
}
