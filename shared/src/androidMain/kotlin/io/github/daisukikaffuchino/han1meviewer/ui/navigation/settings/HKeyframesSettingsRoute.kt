package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeEntity
import io.github.daisukikaffuchino.han1meviewer.ui.component.ConfirmDialog
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.HKeyframeSettingsScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.HKeyframeSettingsUiState
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.HKeyframesScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.SharedHKeyframesScreen
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.SettingsViewModel
import io.github.daisukikaffuchino.utils.rememberCopyTextToClipboard
import io.github.daisukikaffuchino.utils.decodeFromStringByBase64
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.confirm
import han1meviewer.shared.generated.resources.h_keyframes_import_shared
import han1meviewer.shared.generated.resources.h_keyframes_import_shared_hint
import han1meviewer.shared.generated.resources.h_keyframes_shared_by_other_detected
import han1meviewer.shared.generated.resources.copy_to_clipboard
import han1meviewer.shared.generated.resources.delete_success
import han1meviewer.shared.generated.resources.h_keyframes_shared_by_other_not_detected
import han1meviewer.shared.generated.resources.modify_success
import han1meviewer.shared.generated.resources.shared_h_keyframe_detected_msg

@Composable
fun HKeyframesRouteScreen(
    onOpenVideo: (String) -> Unit,
    showImportDialog: Boolean,
    onImportDialogDismiss: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel()
    val copyTextToClipboard = rememberCopyTextToClipboard()
    val items by viewModel.loadAllHKeyframes()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var sharedHKeyframeEntity by remember { mutableStateOf<HKeyframeEntity?>(null) }

    if (showImportDialog) {
        ImportSharedHKeyframeDialog(
            onDismiss = onImportDialogDismiss,
            onConfirm = { content ->
                val entity = parseSharedHKeyframe(content)
                if (entity != null) {
                    sharedHKeyframeEntity = entity
                    onImportDialogDismiss()
                } else {
                    SonnerToast.info(Res.string.h_keyframes_shared_by_other_not_detected)
                }
            },
        )
    }

    HKeyframesScreen(
        items = items,
        onOpenVideo = onOpenVideo,
        onDeleteEntity = { entity ->
            viewModel.deleteHKeyframes(entity)
        },
        onUpdateEntityTitle = { entity, newTitle ->
            viewModel.updateHKeyframes(entity.copy(title = newTitle))
            SonnerToast.success(Res.string.modify_success)
        },
        onDeleteKeyframe = { videoCode, keyframe ->
            viewModel.removeHKeyframe(videoCode, keyframe)
            SonnerToast.success(Res.string.delete_success)
        },
        onUpdateKeyframe = { videoCode, oldKeyframe, newKeyframe ->
            viewModel.modifyHKeyframe(videoCode, oldKeyframe, newKeyframe)
            SonnerToast.success(Res.string.modify_success)
        },
        onCopyShareContent = {
            copyTextToClipboard(it)
            SonnerToast.success(Res.string.copy_to_clipboard)
        },
    )

    sharedHKeyframeEntity?.let { entity ->
        ConfirmDialog(
            visible = true,
            title = stringResource(Res.string.h_keyframes_shared_by_other_detected),
            message = stringResource(
                Res.string.shared_h_keyframe_detected_msg,
                entity.title,
                entity.videoCode,
                entity.keyframes.size,
            ).trimIndent(),
            confirmText = stringResource(Res.string.confirm),
            dismissText = stringResource(Res.string.cancel),
            onConfirm = {
                viewModel.insertHKeyframes(entity.copy(lastModifiedTime = System.currentTimeMillis()))
                sharedHKeyframeEntity = null
            },
            onDismiss = { sharedHKeyframeEntity = null },
        )
    }
}

private val shareRegex = Regex(">>>(.+)<<<")

private fun parseSharedHKeyframe(content: String): HKeyframeEntity? {
    return runCatching {
        val matchResult = shareRegex.find(content) ?: return@runCatching null
        val (toBase64) = matchResult.destructured
        val toJson = toBase64.decodeFromStringByBase64()
        Json.decodeFromString<HKeyframeEntity>(toJson)
    }.getOrNull()
}

@Composable
private fun ImportSharedHKeyframeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.h_keyframes_import_shared)) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(Res.string.h_keyframes_import_shared_hint)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(content) }) {
                Text(stringResource(Res.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
fun SharedHKeyframesRouteScreen(
    onOpenVideo: (String) -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel()
    val items by viewModel.loadAllSharedHKeyframes()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    SharedHKeyframesScreen(
        items = items,
        onOpenVideo = onOpenVideo,
    )
}

@Composable
fun HKeyframeSettingsRouteScreen(
    onNavigateToHKeyframes: () -> Unit,
    onNavigateToSharedHKeyframes: () -> Unit,
    embedded: Boolean = false,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by SettingsRepository.settings.collectAsStateWithLifecycle()
    val uiState = remember(settings, context) { buildHKeyframeSettingsUiState(context) }

    HKeyframeSettingsScreen(
        state = uiState,
        onHKeyframesEnableChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(hKeyframesEnable = it) } }
        },
        onOpenHKeyframeManage = onNavigateToHKeyframes,
        onSharedHKeyframesEnableChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(sharedHKeyframesEnable = it) } }
        },
        onSharedHKeyframesUseFirstChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(sharedHKeyframesUseFirst = it) } }
        },
        onOpenSharedHKeyframeManage = onNavigateToSharedHKeyframes,
        onShowCommentWhenCountdownChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(showCommentWhenCountdown = it) } }
        },
        onWhenCountdownRemindChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(whenCountdownRemindSeconds = it) } }
        },
        embedded = embedded,
    )
}

private fun buildHKeyframeSettingsUiState(context: Context): HKeyframeSettingsUiState {
    return HKeyframeSettingsUiState(
        hKeyframesEnable = SettingsRepository.hKeyframesEnable,
        hKeyframesSummary = if (SettingsRepository.hKeyframesEnable) {
            context.getString(R.string.h_keyframes_enable_tip)
        } else {
            context.getString(R.string.h_keyframes_disable_tip)
        },
        sharedHKeyframesEnable = SettingsRepository.sharedHKeyframesEnable,
        sharedHKeyframesUseFirst = SettingsRepository.sharedHKeyframesUseFirst,
        showCommentWhenCountdown = SettingsRepository.showCommentWhenCountdown,
        whenCountdownRemind = SettingsRepository.whenCountdownRemind / 1000,
        whenCountdownRemindSummary = toPrettyCountdownRemindString(
            context,
            SettingsRepository.whenCountdownRemind / 1000
        ),
    )
}
