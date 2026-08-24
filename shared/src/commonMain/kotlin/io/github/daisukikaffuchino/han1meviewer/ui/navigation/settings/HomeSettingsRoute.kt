package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants
import io.github.daisukikaffuchino.han1meviewer.HA1_GITHUB_FORUM_URL
import io.github.daisukikaffuchino.han1meviewer.HA1_GITHUB_ISSUE_URL
import io.github.daisukikaffuchino.han1meviewer.logic.BackupManager
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.isDeviceSecureCompat
import io.github.daisukikaffuchino.han1meviewer.logic.platform.cacheFolderSize
import io.github.daisukikaffuchino.han1meviewer.logic.platform.cacheFolderSizeBlocking
import io.github.daisukikaffuchino.han1meviewer.logic.platform.clearCacheFolder
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import io.github.daisukikaffuchino.han1meviewer.logic.model.LauncherIconOption
import io.github.daisukikaffuchino.han1meviewer.logic.model.DisplayDensity
import io.github.daisukikaffuchino.han1meviewer.logic.model.PaletteStyle
import io.github.daisukikaffuchino.han1meviewer.logic.model.ThemeAccent
import io.github.daisukikaffuchino.han1meviewer.logic.model.ThemeMode
import io.github.daisukikaffuchino.han1meviewer.logic.model.VideoLandscapeLayoutStyle
import io.github.daisukikaffuchino.han1meviewer.ui.component.ConfirmDialog
import io.github.daisukikaffuchino.han1meviewer.ui.theme.isDynamicColorSupported
import io.github.daisukikaffuchino.han1meviewer.util.switchLauncherIcon
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.HomeSettingsPage
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.HomeSettingsScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.model.HomeSettingsUiState
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.defaultHomeCategoryPreferenceItems
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.hiddenHomeCategoryKeys
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.homeCategoryOrder
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.saveHomeCategoryPreferences
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.utils.currentAppLanguage
import io.github.daisukikaffuchino.utils.selectAppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.current_version
import han1meviewer.shared.generated.resources.follow_system
import han1meviewer.shared.generated.resources.simplified_chinese
import han1meviewer.shared.generated.resources.traditional_chinese
import han1meviewer.shared.generated.resources.app_name_fake_calc
import han1meviewer.shared.generated.resources.app_name_fake_cornhub
import han1meviewer.shared.generated.resources.app_name_fake_xxt
import han1meviewer.shared.generated.resources.apply_deep_links
import han1meviewer.shared.generated.resources.apply_deep_links_summary
import han1meviewer.shared.generated.resources.apply_deep_links_tips
import han1meviewer.shared.generated.resources.attention
import han1meviewer.shared.generated.resources.backup_import_confirm_message
import han1meviewer.shared.generated.resources.backup_import_title
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.confirm
import han1meviewer.shared.generated.resources.fake_app_icon
import han1meviewer.shared.generated.resources.go_to_settings
import han1meviewer.shared.generated.resources.hanime_app_name
import han1meviewer.shared.generated.resources.restart_needed
import han1meviewer.shared.generated.resources.sure_to_clear
import han1meviewer.shared.generated.resources.sure_to_clear_cache
import han1meviewer.shared.generated.resources.action_app_open_by_default_settings_not_support
import han1meviewer.shared.generated.resources.backup_export_failed
import han1meviewer.shared.generated.resources.backup_export_success
import han1meviewer.shared.generated.resources.backup_import_failed
import han1meviewer.shared.generated.resources.backup_import_success
import han1meviewer.shared.generated.resources.cache_empty
import han1meviewer.shared.generated.resources.clear_failed
import han1meviewer.shared.generated.resources.clear_success
import han1meviewer.shared.generated.resources.fake_icon_hint
import han1meviewer.shared.generated.resources.ic_launcher_calc
import han1meviewer.shared.generated.resources.ic_launcher_cornhub
import han1meviewer.shared.generated.resources.ic_launcher_new
import han1meviewer.shared.generated.resources.ic_launcher_xxt
import han1meviewer.shared.generated.resources.not_set_sys_lock
import han1meviewer.shared.generated.resources.request_pip_alert
import han1meviewer.shared.generated.resources.success_value
import org.jetbrains.compose.resources.DrawableResource
import io.github.daisukikaffuchino.han1meviewer.util.canRestartApplication
import io.github.daisukikaffuchino.han1meviewer.util.rememberSetSecureMode
import io.github.daisukikaffuchino.han1meviewer.util.rememberRecreateScreen
import io.github.daisukikaffuchino.han1meviewer.util.rememberOpenDeepLinkSettings
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.daisukikaffuchino.han1meviewer.util.restartApplication
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import org.jetbrains.compose.resources.getString
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsRouteScreen(
    page: HomeSettingsPage,
    onNavigateToHKeyframes: () -> Unit = {},
    onNavigateToSharedHKeyframes: () -> Unit = {},
    onNavigateToOpenSourceLicenses: () -> Unit = {},
) {
    val setSecureMode = rememberSetSecureMode()
    val openDeepLinkSettings = rememberOpenDeepLinkSettings()
    val recreateScreen = rememberRecreateScreen()
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val settings by SettingsRepository.settings.collectAsStateWithLifecycle()
    var cacheKey by remember { mutableIntStateOf(0) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }
    var showRestartConfirmDialog by remember { mutableStateOf(false) }
    var showLauncherPicker by remember { mutableStateOf(false) }
    var showApplyDeepLinksDialog by remember { mutableStateOf(false) }
    var pendingImportFile by remember { mutableStateOf<PlatformFile?>(null) }

    val exportLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        file ?: return@rememberFileSaverLauncher
        coroutineScope.launch(Dispatchers.IO) {
            runCatching { BackupManager.exportTo(file) }
                .onSuccess { withContext(Dispatchers.Main) { SonnerToast.success(Res.string.backup_export_success) } }
                .onFailure { withContext(Dispatchers.Main) { SonnerToast.error(Res.string.backup_export_failed) } }
        }
    }
    val importLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("json")
    ) { file ->
        pendingImportFile = file
    }

    val hanimeAppName = stringResource(Res.string.hanime_app_name)
    val fakeNameCalc = stringResource(Res.string.app_name_fake_calc)
    val fakeNameCornhub = stringResource(Res.string.app_name_fake_cornhub)
    val fakeNameXXT = stringResource(Res.string.app_name_fake_xxt)

    val launcherItems = remember {
        listOf(
            LauncherItem(
                name = hanimeAppName,
                iconRes = Res.drawable.ic_launcher_new,
                alias = LauncherIconOption.Default.alias,
            ),
            LauncherItem(
                name = fakeNameCalc,
                iconRes = Res.drawable.ic_launcher_calc,
                alias = LauncherIconOption.Calc.alias,
            ),
            LauncherItem(
                name = fakeNameCornhub,
                iconRes = Res.drawable.ic_launcher_cornhub,
                alias = LauncherIconOption.Cornhub.alias,
            ),
            LauncherItem(
                name = fakeNameXXT,
                iconRes = Res.drawable.ic_launcher_xxt,
                alias = LauncherIconOption.Xxt.alias,
            ),
        )
    }

    var cacheSummary by remember { mutableStateOf("") }

    LaunchedEffect(cacheKey) {
        cacheSummary = withContext(Dispatchers.IO) {
            generateClearCacheSummary(cacheFolderSize()).toString()
        }
    }
    val uiState = remember(settings, cacheSummary, launcherItems) {
        buildHomeSettingsUiState(
            launcherItems = launcherItems,
            cacheSummary = cacheSummary,
        )
    }

    // 平台没有防截屏能力时保持 null，设置项直接不渲染
    val onSecureModeChange: ((Boolean) -> Unit)? = setSecureMode?.let { applySecureMode ->
        { enabled ->
            coroutineScope.launch {
                SettingsRepository.update { it.copy(secureMode = enabled) }
                applySecureMode(enabled)
            }
        }
    }

    HomeSettingsScreen(
        page = page,
        state = uiState,
        onVideoLanguageChange = { value ->
            if (value != SettingsRepository.videoLanguage) {
                coroutineScope.launch {
                    SettingsRepository.update { it.copy(videoLanguage = value) }
                    showRestartConfirmDialog = true
                }
            }
        },
        onVideoQualityChange = { value ->
            coroutineScope.launch {
                SettingsRepository.update { it.copy(videoQuality = value) }
                SonnerToast.success(Res.string.success_value, value)
            }
        },
        onDarkModeChange = { value ->
            if (value != SettingsRepository.useDarkMode) {
                coroutineScope.launch { SettingsRepository.setThemeMode(ThemeMode.fromValue(value)) }
            }
        },
        onUseDynamicColorChange = { enabled ->
            coroutineScope.launch { SettingsRepository.setDynamicColor(enabled) }
        },
        onHapticFeedbackChange = { enabled ->
            coroutineScope.launch { SettingsRepository.setHapticFeedback(enabled) }
        },
        onFunLoadingHintsChange = { enabled ->
            coroutineScope.launch { SettingsRepository.update { it.copy(funLoadingHints = enabled) } }
        },
        onThemeAccentColorChange = { id ->
            coroutineScope.launch { SettingsRepository.setThemeAccent(ThemeAccent.fromId(id)) }
        },
        onAppPaletteStyleChange = { id ->
            coroutineScope.launch { SettingsRepository.setPaletteStyle(PaletteStyle.fromId(id)) }
        },
        onAllowPipModeChange = { enabled ->
            if (enabled && !isPipPermissionGranted()) {
                SonnerToast.warning(Res.string.request_pip_alert)
                openPipPermissionSettings()
                coroutineScope.launch { SettingsRepository.update { it.copy(allowPipMode = false) } }
                return@HomeSettingsScreen
            }
            coroutineScope.launch { SettingsRepository.update { it.copy(allowPipMode = enabled) } }
        },
        onAllowResumePlaybackChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(allowResumePlayback = it) } }
        },
        onShowPlayedIndicatorChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(showPlayedIndicator = it) } }
        },
        onSearchArtistIgnoreVideoTypeChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(searchArtistIgnoreVideoType = it) } }
        },
        onDisableMobileDataWarningChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(disableMobileDataWarning = it) } }
        },
        onDisablePredictiveBackChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(disablePredictiveBack = it) } }
        },
        onTabletModeChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(tabletMode = it) } }
        },
        onVideoLandscapeLayoutStyleChange = { value ->
            coroutineScope.launch {
                SettingsRepository.setVideoLandscapeLayoutStyle(
                    VideoLandscapeLayoutStyle.fromValue(value)
                )
            }
        },
        onCheckInEnabledChange = {
            coroutineScope.launch {
                SettingsRepository.setCheckInEnabled(it)
                refreshCheckInWidget()
            }
        },
        onDisableCommentsChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(disableComments = it) } }
        },
        onCollapseDownloadedGroupChange = {
            coroutineScope.launch { SettingsRepository.update { settings -> settings.copy(collapseDownloadedGroup = it) } }
        },
        onSearchGridColumnsConfigChange = { config ->
            coroutineScope.launch { SettingsRepository.update { it.copy(searchGridColumnsCompact = config.compactColumns, searchGridColumnsMedium = config.mediumColumns, searchGridColumnsExpanded = config.expandedColumns, searchGridColumnsLarge = config.largeColumns) } }
        },
        onHorizontalCardCountConfigChange = { config ->
            coroutineScope.launch { SettingsRepository.update { it.copy(horizontalCardCountNarrow = config.narrowCount, horizontalCardCountCompact = config.compactCount, horizontalCardCountMedium = config.mediumCount, horizontalCardCountExpanded = config.expandedCount) } }
        },
        onHomeCategoryPreferencesChange = { order, hiddenKeys ->
            coroutineScope.launch { saveHomeCategoryPreferences(order, hiddenKeys) }
        },
        onUseLockScreenChange = { value ->
            if (value) {
                if (!isDeviceSecureCompat()) {
                    SonnerToast.warning(Res.string.not_set_sys_lock)
                    return@HomeSettingsScreen
                }
            }
            coroutineScope.launch { SettingsRepository.update { it.copy(useLockScreen = value) } }
        },
        onSecureModeChange = onSecureModeChange,
        onAlwaysShowUpdateCardChange = { enabled ->
            coroutineScope.launch { SettingsRepository.setAlwaysShowUpdateCard(enabled) }
        },
        onDisplayDensityChange = { percent ->
            coroutineScope.launch {
                SettingsRepository.setDisplayDensity(DisplayDensity.fromPercent(percent))
            }
        },
        onTriggerCrash = {
            throw RuntimeException("Crash triggered from developer options")
        },
        hKeyframeSettingsContent = {
            HKeyframeSettingsRouteScreen(
                onNavigateToHKeyframes = onNavigateToHKeyframes,
                onNavigateToSharedHKeyframes = onNavigateToSharedHKeyframes,
                embedded = true,
            )
        },
        networkSettingsContent = { NetworkSettingsRouteScreen(embedded = true) },
        downloadSettingsContent = { DownloadSettingsRouteScreen(embedded = true) },
        onOpenAppLanguageSettings = { value ->
            val language = AppLanguage.fromPreference(value)
            if (currentAppLanguage() != language) {
                coroutineScope.launch {
                    if (!selectAppLanguage(language)) return@launch
                    // iOS 没法自己重启，只能提示用户手动重开
                    if (canRestartApplication) {
                        showRestartConfirmDialog = true
                    } else {
                        SonnerToast.info(Res.string.restart_needed)
                    }
                }
            }
        },
        onOpenApplyDeepLinks = {
            if (openDeepLinkSettings == null) {
                SonnerToast.warning(Res.string.action_app_open_by_default_settings_not_support)
            } else {
                showApplyDeepLinksDialog = true
            }
        },
        onOpenFakeLauncherIcon = { showLauncherPicker = true },
        onOpenOpenSourceLicense = onNavigateToOpenSourceLicenses,
        onClearCache = {
            if (cacheFolderSizeBlocking() == 0L) {
                SonnerToast.info(Res.string.cache_empty)
                return@HomeSettingsScreen
            }
            showClearCacheConfirm = true
        },
        onExportBackup = {
            exportLauncher.launch(
                "Han1meViewer-backup-${Clock.System.now().toEpochMilliseconds()}",
                "json",
            )
        },
        onImportBackup = {
            importLauncher.launch()
        },
        onSubmitBug = { uriHandler.openUri(HA1_GITHUB_ISSUE_URL) },
        onOpenForum = { uriHandler.openUri(HA1_GITHUB_FORUM_URL) },
    )

    ConfirmDialog(
        visible = pendingImportFile != null,
        title = stringResource(Res.string.backup_import_title),
        message = stringResource(Res.string.backup_import_confirm_message),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = {
            val file = pendingImportFile ?: return@ConfirmDialog
            pendingImportFile = null
            coroutineScope.launch(Dispatchers.IO) {
                runCatching { BackupManager.importFrom(file) }
                    .onSuccess {
                        withContext(Dispatchers.Main) {
                            SonnerToast.success(Res.string.backup_import_success)
                            recreateScreen?.invoke()
                        }
                    }
                    .onFailure {
                        withContext(Dispatchers.Main) {
                            SonnerToast.error(Res.string.backup_import_failed)
                        }
                    }
            }
        },
        onDismiss = { pendingImportFile = null },
    )

    ConfirmDialog(
        visible = showClearCacheConfirm,
        title = stringResource(Res.string.sure_to_clear),
        message = stringResource(Res.string.sure_to_clear_cache),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = {
            showClearCacheConfirm = false
            coroutineScope.launch(Dispatchers.IO) {
                val success = clearCacheFolder()
                withContext(Dispatchers.Main) {
                    cacheKey++
                    if (success) SonnerToast.success(Res.string.clear_success) else SonnerToast.error(Res.string.clear_failed)
                }
            }
        },
        onDismiss = { showClearCacheConfirm = false },
    )

    if (showApplyDeepLinksDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDeepLinksDialog = false },
            title = { Text(stringResource(Res.string.apply_deep_links)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(Res.string.apply_deep_links_summary))
                    Text(stringResource(Res.string.apply_deep_links_tips))
                    Image(
                        painter = painterResource(Res.drawable.apply_deep_links),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showApplyDeepLinksDialog = false
                        openDeepLinkSettings?.invoke()
                    },
                ) {
                    Text(stringResource(Res.string.go_to_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDeepLinksDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    ConfirmDialog(
        visible = showRestartConfirmDialog,
        title = stringResource(Res.string.attention),
        message = stringResource(Res.string.restart_needed),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        cancelable = false,
        onConfirm = {
            restartApplication()
        },
        onDismiss = { showRestartConfirmDialog = false },
    )

    if (showLauncherPicker) {
        Dialog(
            onDismissRequest = { showLauncherPicker = false },
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(Res.string.fake_app_icon),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    launcherItems.forEach { item ->
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    SettingsRepository.setLauncherIcon(item.alias)
                                    switchLauncherIcon(item.alias)
                                    SonnerToast.info(Res.string.fake_icon_hint)
                                    showLauncherPicker = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(30.dp),
                                )
                                Text(item.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class LauncherItem(
    val name: String,
    val iconRes: DrawableResource,
    val alias: String,
)

private fun buildHomeSettingsUiState(
    launcherItems: List<LauncherItem>,
    cacheSummary: String,
): HomeSettingsUiState = runBlocking {
    val currentAlias = SettingsRepository.fakeLauncherIcon
    val currentItem = launcherItems.find { it.alias == currentAlias } ?: launcherItems.first()
    val videoLanguageLabel = when (SettingsRepository.videoLanguage) {
        "zht" -> getString(Res.string.traditional_chinese)
        "zhs" -> getString(Res.string.simplified_chinese)
        else -> SettingsRepository.videoLanguage
    }
    val appLanguage = currentAppLanguage()
    val appLanguageLabel = when (appLanguage) {
        AppLanguage.SYSTEM -> getString(Res.string.follow_system)
        AppLanguage.ENGLISH -> "English"
        AppLanguage.CHINESE_SIMPLIFIED -> "简体中文"
        AppLanguage.CHINESE_TRADITIONAL -> "繁體中文"
    }
    val searchGridColumnsConfig = SettingsRepository.searchGridColumnsConfig
    val horizontalCardCountConfig = SettingsRepository.horizontalCardCountConfig
    HomeSettingsUiState(
        videoLanguage = SettingsRepository.videoLanguage,
        videoLanguageLabel = videoLanguageLabel,
        defaultVideoQuality = SettingsRepository.videoQuality,
        darkMode = SettingsRepository.useDarkMode,
        appLanguage = appLanguage.preferenceValue,
        appLanguageLabel = appLanguageLabel,
        allowPipMode = SettingsRepository.current.allowPipMode,
        allowResumePlayback = SettingsRepository.allowResumePlayback,
        showPlayedIndicator = SettingsRepository.showPlayedIndicator,
        searchArtistIgnoreVideoType = SettingsRepository.searchArtistIgnoreVideoType,
        disableMobileDataWarning = SettingsRepository.disableMobileDataWarning,
        disablePredictiveBack = SettingsRepository.disablePredictiveBack,
        tabletMode = SettingsRepository.tabletMode,
        videoLandscapeLayoutStyle = SettingsRepository.videoLandscapeLayoutStyle.value,
        disableComments = SettingsRepository.current.disableComments,
        collapseDownloadedGroup = SettingsRepository.collapseDownloadedGroup,
        useDynamicColor = SettingsRepository.useDynamicColor,
        hapticFeedbackEnabled = SettingsRepository.hapticFeedbackEnabled,
        funLoadingHints = SettingsRepository.funLoadingHints,
        useLockScreen = SettingsRepository.current.useLockScreen,
        secureMode = SettingsRepository.secureMode,
        fakeLauncherIconName = currentItem.name,
        cacheSummary = cacheSummary,
        versionSummary = getString(
            Res.string.current_version,
            "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
        ),
        dynamicColorEnabled = isDynamicColorSupported(),
        themeAccentColorId = SettingsRepository.current.themeAccent.id,
        appPaletteStyleId = SettingsRepository.current.paletteStyle.id,
        searchGridColumnsSummary = listOf(
            searchGridColumnsConfig.compactColumns,
            searchGridColumnsConfig.mediumColumns,
            searchGridColumnsConfig.expandedColumns,
            searchGridColumnsConfig.largeColumns,
        ).joinToString(" / "),
        searchGridColumnsConfig = searchGridColumnsConfig,
        horizontalCardCountSummary = "${horizontalCardCountConfig.narrowCount}~${horizontalCardCountConfig.expandedCount}",
        horizontalCardCountConfig = horizontalCardCountConfig,
        checkInEnabled = SettingsRepository.isCheckInEnabled,
        homeCategoryItems = defaultHomeCategoryPreferenceItems,
        homeCategoryOrder = homeCategoryOrder,
        hiddenHomeCategoryKeys = hiddenHomeCategoryKeys,
        useAvHomeCategoryTitles = SettingsRepository.baseUrl == HanimeConstants.HANIME_URL[3],
        alwaysShowUpdateCard = SettingsRepository.alwaysShowUpdateCard,
        displayDensityPercent = SettingsRepository.displayDensity.percent,
    )
}

/** 签到桌面小组件，只有 Android 有。 */
expect suspend fun refreshCheckInWidget()
