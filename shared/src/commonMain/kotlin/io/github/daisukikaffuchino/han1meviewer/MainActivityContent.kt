package io.github.daisukikaffuchino.han1meviewer

import io.github.daisukikaffuchino.utils.LogUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboard
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.StorageSwitchNotice
import io.github.daisukikaffuchino.han1meviewer.logic.exception.CloudflareBlockedException
import io.github.daisukikaffuchino.han1meviewer.logic.state.PageState
import io.github.daisukikaffuchino.han1meviewer.ui.component.UsageNoticeDialog
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticTextButton as TextButton
import io.github.daisukikaffuchino.han1meviewer.ui.component.ConfirmDialog
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.HomeRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.MainDrawerDestination
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.TopNavigation
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.VideoRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.navigateDrawerDestination
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.HomePageViewModel
import io.github.daisukikaffuchino.han1meviewer.videoUrlRegex
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.coroutines.launch
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.app_source_confirm
import han1meviewer.shared.generated.resources.app_source_douyin_tiktok
import han1meviewer.shared.generated.resources.app_source_forum
import han1meviewer.shared.generated.resources.app_source_github
import han1meviewer.shared.generated.resources.app_source_illegal_message
import han1meviewer.shared.generated.resources.app_source_illegal_title
import han1meviewer.shared.generated.resources.app_source_qq_group
import han1meviewer.shared.generated.resources.app_source_repository_link
import han1meviewer.shared.generated.resources.app_source_telegram
import han1meviewer.shared.generated.resources.app_source_title
import han1meviewer.shared.generated.resources.app_source_verify
import han1meviewer.shared.generated.resources.app_source_wechat
import han1meviewer.shared.generated.resources.confirm_switch_site
import han1meviewer.shared.generated.resources.no
import han1meviewer.shared.generated.resources.save_failed_message
import han1meviewer.shared.generated.resources.save_failed_title
import han1meviewer.shared.generated.resources.sure
import han1meviewer.shared.generated.resources.sure_to_logout
import han1meviewer.shared.generated.resources.understood
import han1meviewer.shared.generated.resources.login_first
import io.github.daisukikaffuchino.han1meviewer.util.NavigationEvent
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.AccountRoute
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.CommentViewModel
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.window.core.layout.WindowSizeClass
import han1meviewer.shared.generated.resources.detect_ha1_related_link_in_clipboard
import han1meviewer.shared.generated.resources.enter
import io.github.daisukikaffuchino.han1meviewer.ui.screen.main.MainActivityScaffold
import io.github.daisukikaffuchino.han1meviewer.util.rememberExitApp
import io.github.daisukikaffuchino.utils.rememberReadClipboardText
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    viewModel: HomePageViewModel = koinViewModel(),
) {
    val backStack = viewModel.mainBackStack
    val readClipboardText = rememberReadClipboardText()
    val exitApp = rememberExitApp()
    // NavDisplay 之外创建，预览页和评论页共用同一个实例
    val commentViewModel: CommentViewModel = koinViewModel()
    val showAuthGuard = viewModel.showAuthGuard
    val showSiteSwitchConfirm = viewModel.showSiteSwitchConfirm
    val logoutDialogCloseCurrentPage = viewModel.logoutDialogCloseCurrentPage
    val onOpenAccount = { backStack.add(AccountRoute) }
    val onLogoutClick = { viewModel.showLogoutConfirmDialog() }
    val onRequireLogin = { viewModel.openLogin() }
    val onSwitchSiteClick = { viewModel.requestSiteSwitch() }
    val onDismissSiteSwitch = { viewModel.dismissSiteSwitch() }
    val onConfirmSiteSwitch = { viewModel.confirmSiteSwitch() }
    val onDismissLogout = { viewModel.dismissLogoutDialog() }
    val onConfirmLogout = { viewModel.confirmLogout() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showUsageNotice by remember { mutableStateOf(!SettingsRepository.usageNoticeAccepted) }
    var showSourceDialog by remember {
        mutableStateOf(
            SettingsRepository.usageNoticeAccepted &&
                    !SettingsRepository.usageSourceVerified &&
                    !SettingsRepository.usageSourcePending,
        )
    }
    var showSourceWarning by rememberSaveable {
        mutableStateOf(
            SettingsRepository.usageNoticeAccepted &&
                    !SettingsRepository.usageSourceVerified &&
                    SettingsRepository.usageSourcePending,
        )
    }
    var sourceLink by rememberSaveable { mutableStateOf("") }
    var appAccessGranted by remember {
        mutableStateOf(SettingsRepository.usageNoticeAccepted && SettingsRepository.usageSourceVerified)
    }
    val isDrawerOpen =
        drawerState.currentValue == DrawerValue.Open || drawerState.targetValue == DrawerValue.Open

    val homeState by viewModel.homePageFlow.collectAsStateWithLifecycle()
    val showStorageSwitchNotice by StorageSwitchNotice.visible.collectAsStateWithLifecycle()
    val isLoggedIn by SettingsRepository.loginStateFlow.collectAsStateWithLifecycle()
    val checkInEnabled by SettingsRepository.checkInEnabledFlow.collectAsStateWithLifecycle()
    val headerAvatarUrl = if (isLoggedIn) {
        (homeState as? PageState.Success)?.info?.page?.avatarUrl
    } else {
        null
    }
    val headerUsername = if (isLoggedIn) {
        (homeState as? PageState.Success)?.info?.page?.username
    } else {
        null
    }
    val headerIsLoading = isLoggedIn && homeState is PageState.Loading
    val currentRoute = backStack.currentKey
    val previousRoute = backStack.backStack.getOrNull(backStack.backStack.lastIndex - 1)
    val selectedDrawerDestination = MainDrawerDestination.fromRoute(backStack.topLevelKey)
    val drawerEnabled = currentRoute == HomeRoute
    val isLandscape = !currentWindowAdaptiveInfoV2().windowSizeClass
        .isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val permanentDrawer = (drawerEnabled || previousRoute == HomeRoute) && isLandscape
    LaunchedEffect(permanentDrawer) {
        if (permanentDrawer) drawerState.close()
    }
    LaunchedEffect(Unit) {
        val clipboardText = readClipboardText()
        val videoCode = clipboardText?.let { videoUrlRegex.find(it)?.groupValues?.get(1) }
        if (videoCode != null) {
            val result = snackbarHostState.showSnackbar(
                message = getString(Res.string.detect_ha1_related_link_in_clipboard),
                actionLabel = getString(Res.string.enter),
                withDismissAction = true,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.openVideo(videoCode)
            }
        }
    }
    LaunchedEffect(Unit) {
        NavigationEvent.event.collect { screen ->
            backStack.add(screen)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.sessionExpiredMessage.collect { event ->
            event.message?.let(SonnerToast::error) ?: SonnerToast.error(event.fallbackResId)
        }
    }
    LaunchedEffect(homeState) {
        if (homeState is PageState.Error) {
            val throwable = (homeState as PageState.Error).throwable
            if (throwable is CloudflareBlockedException) {
                LogUtil.e("error", "被屏蔽时的处理")
            }
        }
    }
    MainActivityScaffold(
        drawerState = drawerState,
        drawerEnabled = drawerEnabled,
        permanentDrawer = permanentDrawer,
        applyHorizontalSafeInsets = currentRoute !is VideoRoute,
        selectedDestination = selectedDrawerDestination,
        avatarUrl = headerAvatarUrl,
        username = headerUsername,
        isLoggedIn = isLoggedIn,
        isLoading = headerIsLoading,
        currentSite = SettingsRepository.baseUrl,
        checkInEnabled = checkInEnabled,
        onAvatarClick = {
            if (isLoggedIn) {
                scope.launch { drawerState.close() }
                onOpenAccount()
            } else {
                scope.launch {
                    drawerState.close()
                    onRequireLogin()
                }
            }
        },
        onAvatarLongClick = {
            onLogoutClick()
        },
        onSwitchSiteClick = onSwitchSiteClick,
        onDrawerItemSelected = { destination ->
            val handled = backStack.navigateDrawerDestination(
                destination = destination,
                isLoggedIn = isLoggedIn,
                onRequireLogin = { SonnerToast.warning(Res.string.login_first) },
            )
            if (handled) {
                scope.launch { drawerState.close() }
            }
            handled
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (appAccessGranted) {
                TopNavigation(
                    viewModel = viewModel,
                    commentViewModel = commentViewModel,
                    backStack = backStack,
                    isDrawerOpen = isDrawerOpen && !permanentDrawer,
                    showHomeNavigationIcon = !permanentDrawer,
                    homeContentStartPadding = if (permanentDrawer) 280.dp else 0.dp,
                    onOpenDrawer = {
                        if (drawerEnabled) {
                            scope.launch { drawerState.open() }
                        }
                    },
                )
            }
            if (showAuthGuard) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                )
            }
            UsageNoticeDialog(
                visible = showUsageNotice,
                onAccepted = {
                    scope.launch {
                        SettingsRepository.setUsageNoticeAccepted(true)
                        showUsageNotice = false
                        if (SettingsRepository.usageSourceVerified) {
                            appAccessGranted = true
                            viewModel.initializeHomePage()
                        } else if (SettingsRepository.usageSourcePending) {
                            showSourceWarning = true
                        } else {
                            showSourceDialog = true
                        }
                    }
                },
                onDeclined = { exitApp() },
            )
            AppSourceDialog(
                visible = showSourceDialog,
                onSelect = { source ->
                    if (source.equals("github", ignoreCase = true)) {
                        scope.launch {
                            SettingsRepository.update {
                                it.copy(
                                    usageSourceVerified = true,
                                    usageSourcePending = false
                                )
                            }
                            showSourceDialog = false
                            appAccessGranted = true
                            viewModel.initializeHomePage()
                        }
                    } else {
                        scope.launch {
                            SettingsRepository.setUsageSourcePending(true)
                            showSourceDialog = false
                            sourceLink = ""
                            showSourceWarning = true
                        }
                    }
                },
            )
            if (showSourceWarning) {
                val expectedRepository = "https://github.com/daisukiKaffuChino/Han1meViewer"
                val linkValid = sourceLink.trim().equals(expectedRepository, ignoreCase = true)
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(stringResource(Res.string.app_source_illegal_title)) },
                    text = {
                        Column {
                            Text(stringResource(Res.string.app_source_illegal_message))
                            OutlinedTextField(
                                value = sourceLink,
                                onValueChange = { sourceLink = it },
                                label = { Text(stringResource(Res.string.app_source_repository_link)) },
                                singleLine = true,
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            enabled = linkValid,
                            onClick = {
                                scope.launch {
                                    SettingsRepository.update {
                                        it.copy(
                                            usageSourceVerified = true,
                                            usageSourcePending = false
                                        )
                                    }
                                    showSourceWarning = false
                                    appAccessGranted = true
                                    viewModel.initializeHomePage()
                                }
                            },
                        ) { Text(stringResource(Res.string.app_source_verify)) }
                    },
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
    ConfirmDialog(
        visible = showSiteSwitchConfirm,
        title = stringResource(Res.string.confirm_switch_site),
        message = "",
        confirmText = stringResource(Res.string.sure),
        dismissText = stringResource(Res.string.no),
        onConfirm = onConfirmSiteSwitch,
        onDismiss = onDismissSiteSwitch,
    )
    ConfirmDialog(
        visible = logoutDialogCloseCurrentPage != null,
        title = stringResource(Res.string.sure_to_logout),
        message = "",
        confirmText = stringResource(Res.string.sure),
        dismissText = stringResource(Res.string.no),
        onConfirm = onConfirmLogout,
        onDismiss = onDismissLogout,
    )
    ConfirmDialog(
        visible = showStorageSwitchNotice,
        title = stringResource(Res.string.save_failed_title),
        message = stringResource(Res.string.save_failed_message),
        confirmText = stringResource(Res.string.understood),
        dismissText = null,
        onConfirm = StorageSwitchNotice::dismiss,
        onDismiss = StorageSwitchNotice::dismiss,
    )
}

@Composable
private fun AppSourceDialog(
    visible: Boolean,
    onSelect: (String) -> Unit,
) {
    if (!visible) return

    var selectedSource by rememberSaveable { mutableStateOf<String?>(null) }
    val options = listOf(
        stringResource(Res.string.app_source_forum) to "forum",
        stringResource(Res.string.app_source_telegram) to "telegram",
        stringResource(Res.string.app_source_github) to "github",
        stringResource(Res.string.app_source_qq_group) to "qq_group",
        stringResource(Res.string.app_source_wechat) to "wechat",
        stringResource(Res.string.app_source_douyin_tiktok) to "douyin_tiktok",
    )
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(Res.string.app_source_title)) },
        text = {
            Column {
                options.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSource = value }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedSource == value,
                            onClick = { selectedSource = value },
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedSource != null,
                onClick = { selectedSource?.let(onSelect) },
            ) { Text(stringResource(Res.string.app_source_confirm)) }
        },
    )
}
