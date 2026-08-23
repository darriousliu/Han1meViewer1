package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.confirm_import
import han1meviewer.shared.generated.resources.default_path_restored
import han1meviewer.shared.generated.resources.directory_saved
import han1meviewer.shared.generated.resources.import_complete
import han1meviewer.shared.generated.resources.import_progress
import han1meviewer.shared.generated.resources.import_progress_format
import han1meviewer.shared.generated.resources.import_warning
import han1meviewer.shared.generated.resources.importing
import han1meviewer.shared.generated.resources.no_directory_selected
import han1meviewer.shared.generated.resources.no_exportable_files
import han1meviewer.shared.generated.resources.ok
import han1meviewer.shared.generated.resources.path_permission_message
import han1meviewer.shared.generated.resources.permission_error
import han1meviewer.shared.generated.resources.restore_default_message
import han1meviewer.shared.generated.resources.restore_default_path
import han1meviewer.shared.generated.resources.select_download_folder
import han1meviewer.shared.generated.resources.select_folder_message
import han1meviewer.shared.generated.resources.specify_path_first
import han1meviewer.shared.generated.resources.understood
import han1meviewer.shared.generated.resources.unknown_error
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.dao.DownloadDatabase
import io.github.daisukikaffuchino.han1meviewer.logic.model.DOWNLOAD_SPEED_BYTES
import io.github.daisukikaffuchino.han1meviewer.ui.component.ConfirmDialog
import io.github.daisukikaffuchino.han1meviewer.ui.component.TripleButtonDialog
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.DownloadSettingsScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.DownloadSettingsUiState
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.sergeych.sprintf.sprintf
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticTextButton as TextButton
import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao
import io.github.vinceglb.filekit.PlatformFile

@Composable
fun DownloadSettingsRouteScreen(embedded: Boolean = false) {
    val coroutineScope = rememberCoroutineScope()
    val settings by SettingsRepository.settings.collectAsStateWithLifecycle()
    var showDownloadPathDialog by remember { mutableStateOf(false) }
    var showRestoreDefaultConfirm by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var showSpecifyPathDialog by remember { mutableStateOf(false) }
    var importProgress by remember { mutableStateOf<ImportProgress?>(null) }
    val dao = remember { DownloadDatabase.instance.hanimeDownloadDao }
    val uiState = remember(settings) { buildDownloadSettingsUiState() }

    val openDirectoryPicker = rememberDirectoryPickerLauncher { file ->
        if (file != null) {
            coroutineScope.launch {
                persistDownloadDirectory(file)
                SonnerToast.success(Res.string.directory_saved, file.toString())
            }
        } else {
            SonnerToast.warning(Res.string.no_directory_selected)
        }
    }

    DownloadSettingsScreen(
        state = uiState,
        maxDownloadCountLimit = 10,
        maxDownloadSpeedLimitIndex = DOWNLOAD_SPEED_BYTES.lastIndex,
        onOpenDownloadPath = { showDownloadPathDialog = true },
        onRestoreDefaultPath = { },
        onImportDownloadedFiles = if (!isDownloadMigrationSupported) null else {
            {
                if (!SettingsRepository.isUsePrivateStorage &&
                    !SettingsRepository.safDownloadPath.isNullOrBlank() &&
                    hasDownloadDirectoryPermission()
                ) {
                    showImportConfirm = true
                } else {
                    showSpecifyPathDialog = true
                }
            }
        },
        onDownloadCountLimitChange = { value ->
            coroutineScope.launch {
                SettingsRepository.setDownloadCountLimit(value)
                setMaxConcurrentDownloadCount(value)
            }
        },
        onDownloadSpeedLimitChange = { value ->
            coroutineScope.launch { SettingsRepository.setDownloadSpeedLimitIndex(value) }
        },
        embedded = embedded,
    )

    if (!SettingsRepository.isUsePrivateStorage) {
        TripleButtonDialog(
            visible = showDownloadPathDialog,
            title = stringResource(Res.string.select_download_folder),
            message = stringResource(Res.string.select_folder_message),
            negativeText = stringResource(Res.string.cancel),
            neutralText = stringResource(Res.string.restore_default_path),
            positiveText = stringResource(Res.string.ok),
            onNegative = { showDownloadPathDialog = false },
            onNeutral = {
                showDownloadPathDialog = false
                showRestoreDefaultConfirm = true
            },
            onPositive = {
                showDownloadPathDialog = false
                openDirectoryPicker.launch()
            },
            onDismiss = { showDownloadPathDialog = false },
        )
    } else {
        ConfirmDialog(
            visible = showDownloadPathDialog,
            title = stringResource(Res.string.select_download_folder),
            message = stringResource(Res.string.select_folder_message),
            confirmText = stringResource(Res.string.ok),
            dismissText = stringResource(Res.string.cancel),
            onConfirm = {
                showDownloadPathDialog = false
                openDirectoryPicker.launch()
            },
            onDismiss = { showDownloadPathDialog = false },
        )
    }

    ConfirmDialog(
        visible = showRestoreDefaultConfirm,
        title = stringResource(Res.string.restore_default_path),
        message = stringResource(Res.string.restore_default_message),
        confirmText = stringResource(Res.string.ok),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = {
            coroutineScope.launch {
                SettingsRepository.setDownloadStorage(usePrivate = true, path = null)
                showRestoreDefaultConfirm = false
                SonnerToast.success(Res.string.default_path_restored)
            }
        },
        onDismiss = { showRestoreDefaultConfirm = false },
    )

    ConfirmDialog(
        visible = showImportConfirm,
        title = stringResource(Res.string.confirm_import),
        message = stringResource(Res.string.import_warning),
        confirmText = stringResource(Res.string.ok),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = {
            showImportConfirm = false
            importProgress = ImportProgress()
            migrateDownloadsToPublicStorage(dao) { migrated, total ->
                when (total) {
                    0 -> {
                        importProgress = null
                        SonnerToast.info(Res.string.no_exportable_files)
                    }

                    -1 -> {
                        importProgress = null
                        SonnerToast.error(Res.string.permission_error)
                    }

                    else -> {
                        importProgress = ImportProgress(migrated, total)
                        if (migrated == total) {
                            importProgress = null
                            SonnerToast.success(Res.string.import_complete, total)
                        }
                    }
                }
            }
        },
        onDismiss = { showImportConfirm = false },
    )

    if (showSpecifyPathDialog) {
        AlertDialog(
            onDismissRequest = { showSpecifyPathDialog = false },
            title = { Text(stringResource(Res.string.specify_path_first)) },
            text = { Text(stringResource(Res.string.path_permission_message)) },
            confirmButton = {
                TextButton(onClick = { showSpecifyPathDialog = false }) {
                    Text(stringResource(Res.string.understood))
                }
            },
        )
    }

    importProgress?.let { progress ->
        ImportProgressDialog(progress = progress)
    }
}

private data class ImportProgress(
    val migrated: Int = 0,
    val total: Int = 0,
)

@Composable
private fun ImportProgressDialog(progress: ImportProgress) {
    val percent = if (progress.total > 0) {
        progress.migrated * 100 / progress.total
    } else {
        0
    }
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(Res.string.import_progress),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(stringResource(Res.string.importing))
                LinearProgressIndicator(
                    progress = {
                        if (progress.total > 0) {
                            progress.migrated.toFloat() / progress.total.toFloat()
                        } else {
                            0f
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(
                        Res.string.import_progress_format,
                        progress.migrated,
                        progress.total,
                        "%d%%".sprintf(percent),
                    )
                )
            }
        }
    }
}

private fun buildDownloadSettingsUiState(): DownloadSettingsUiState = runBlocking {
    val pathSummary = getDownloadPath() ?: return@runBlocking DownloadSettingsUiState(
        downloadPathSummary = getString(Res.string.unknown_error),
        downloadCountLimit = SettingsRepository.downloadCountLimit,
        downloadCountLimitSummary = toDownloadCountLimitPrettyString(SettingsRepository.downloadCountLimit),
        downloadSpeedLimitIndex = SettingsRepository.current.downloadSpeedLimitIndex,
        downloadSpeedLimitSummary = DOWNLOAD_SPEED_BYTES[
            SettingsRepository.current.downloadSpeedLimitIndex
        ].toDownloadSpeedPrettyString(),
    )
    val speedIndex = SettingsRepository.current.downloadSpeedLimitIndex
    DownloadSettingsUiState(
        downloadPathSummary = pathSummary,
        downloadCountLimit = SettingsRepository.downloadCountLimit,
        downloadCountLimitSummary = toDownloadCountLimitPrettyString(
            SettingsRepository.downloadCountLimit
        ),
        downloadSpeedLimitIndex = speedIndex,
        downloadSpeedLimitSummary = DOWNLOAD_SPEED_BYTES[speedIndex]
            .toDownloadSpeedPrettyString(),
    )
}

expect fun getDownloadPath(): String?

/** 选好目录后持久化下来（Android 上是 SAF 的 uri 授权）。 */
expect suspend fun persistDownloadDirectory(file: PlatformFile)

/** 已保存的下载目录当前是否还可写。 */
expect fun hasDownloadDirectoryPermission(): Boolean

expect fun setMaxConcurrentDownloadCount(value: Int)

/** 只有 Android 分「应用私有目录 / 公共目录」，其余平台隐藏迁移入口。 */
expect val isDownloadMigrationSupported: Boolean

/**
 * 把私有目录里的下载迁到用户选的公共目录。
 * onProgress 的 total 为 0 表示没有可迁移的文件，-1 表示没权限。
 */
expect fun migrateDownloadsToPublicStorage(
    dao: HanimeDownloadDao?,
    onProgress: (migrated: Int, total: Int) -> Unit,
)
