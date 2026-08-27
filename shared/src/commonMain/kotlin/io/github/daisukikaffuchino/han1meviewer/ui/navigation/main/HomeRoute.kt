package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.getHanimeShareText
import io.github.daisukikaffuchino.han1meviewer.logic.DatabaseRepo
import io.github.daisukikaffuchino.han1meviewer.logic.entity.CheckInType
import io.github.daisukikaffuchino.han1meviewer.logic.model.Announcement
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.component.AnnouncementDialog
import io.github.daisukikaffuchino.han1meviewer.ui.component.ConfirmDialog
import io.github.daisukikaffuchino.han1meviewer.ui.component.TripleButtonDialog
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.HomePageScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.HomeUiEvent
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.LocalSearchHistoryQuery
import io.github.daisukikaffuchino.utils.rememberCopyTextToClipboard
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.CheckInCalendarViewModel
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.coroutines.flow.first
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.checkout_exit
import han1meviewer.shared.generated.resources.confirm_exit_message
import han1meviewer.shared.generated.resources.confirm_to_exit
import han1meviewer.shared.generated.resources.do_more
import han1meviewer.shared.generated.resources.exit
import han1meviewer.shared.generated.resources.finished_masturbating
import han1meviewer.shared.generated.resources.copy_to_clipboard
import han1meviewer.shared.generated.resources.update_link_open_failed
import han1meviewer.shared.generated.resources.update_in_app_failed
import io.github.daisukikaffuchino.han1meviewer.logic.update.InAppUpdateStage
import io.github.daisukikaffuchino.han1meviewer.logic.update.runInAppUpdate
import io.github.daisukikaffuchino.han1meviewer.logic.update.supportsInAppUpdate
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.component.UpdateProgressDialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.daisukikaffuchino.han1meviewer.util.nowTime
import io.github.daisukikaffuchino.han1meviewer.util.today
import io.github.daisukikaffuchino.han1meviewer.util.toHourMinuteString
import io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage.HomePageViewModel
import io.github.daisukikaffuchino.han1meviewer.util.rememberExitApp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRouteScreen(
    viewModel: HomePageViewModel,
    isDrawerOpen: Boolean,
    showNavigationIcon: Boolean,
    onOpenDrawer: () -> Unit,
    onNavigateToPreview: () -> Unit,
    onNavigateToSearch: (String?) -> Unit,
    onNavigateToSearchAdvanced: (Map<String, String>) -> Unit,
    onNavigateToVideo: (String) -> Unit,
) {
    val exitApp = rememberExitApp()
    val checkInEnabled by SettingsRepository.checkInEnabledFlow.collectAsStateWithLifecycle()
    val checkInViewModel: CheckInCalendarViewModel? = if (checkInEnabled) koinViewModel() else null
    val copyTextToClipboard = rememberCopyTextToClipboard()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    // 非空表示应用内更新正在进行；安装成功后进程会被重启，不需要复位
    var updateStage by remember { mutableStateOf<InAppUpdateStage?>(null) }
    val openUpdatePage: (String) -> Unit = { url ->
        runCatching { uriHandler.openUri(url) }
            .onFailure { SonnerToast.error(Res.string.update_link_open_failed) }
    }
    val confirmToExit = stringResource(Res.string.confirm_to_exit)
    val confirmExitMessage = stringResource(Res.string.confirm_exit_message)
    val cancel = stringResource(Res.string.cancel)
    val exit = stringResource(Res.string.exit)
    var showExitDialog by remember { mutableStateOf(false) }
    var announcement by remember { mutableStateOf<Announcement?>(null) }
    CompositionLocalProvider(
        LocalSearchHistoryQuery provides { keyword: String ->
            DatabaseRepo.SearchHistory.loadAll(keyword).first().map { it.query }
        }
    ) {
        HomePageScreen(
            viewModel = viewModel,
            isDrawerOpen = isDrawerOpen,
            showNavigationIcon = showNavigationIcon,
            onEvent = { event ->
                when (event) {
                    is HomeUiEvent.OpenDrawer -> onOpenDrawer()
                    is HomeUiEvent.NavigateToPreview -> onNavigateToPreview()
                    is HomeUiEvent.OpenSearchPage -> onNavigateToSearch(event.query)
                    is HomeUiEvent.NavigateToSearchAdvanced -> onNavigateToSearchAdvanced(event.params)
                    is HomeUiEvent.OpenVideo -> onNavigateToVideo(event.videoCode)
                    is HomeUiEvent.LongPressVideoCopy -> {
                        copyTextToClipboard(getHanimeShareText(event.videoTitle, event.videoCode))
                        SonnerToast.success(Res.string.copy_to_clipboard)
                    }
                    is HomeUiEvent.ShowAnnouncementDialog -> { announcement = event.announcement }
                    is HomeUiEvent.ShowExitDialog -> { showExitDialog = true }
                    is HomeUiEvent.OpenUpdatePage -> {
                        // 桌面端（且当前发行形态装得了更新）走 Nucleus 的差分下载 + 安装重启，
                        // 其余平台、以及应用内更新走不通时，退回原来打开下载页那条路。
                        if (supportsInAppUpdate) {
                            scope.launch {
                                runInAppUpdate { updateStage = it }
                                    .onFailure {
                                        updateStage = null
                                        SonnerToast.warning(Res.string.update_in_app_failed)
                                        openUpdatePage(event.downloadUrl)
                                    }
                            }
                        } else {
                            openUpdatePage(event.downloadUrl)
                        }
                    }
                    is HomeUiEvent.IgnoreUpdate -> viewModel.ignoreUpdate(event.versionCode)
                }
            }
        )
    }

    updateStage?.let { UpdateProgressDialog(it) }

    if (showExitDialog && checkInEnabled) {
        TripleButtonDialog(
            visible = true,
            title = confirmToExit,
            message = stringResource(Res.string.finished_masturbating),
            negativeText = stringResource(Res.string.do_more),
            neutralText = stringResource(Res.string.checkout_exit),
            positiveText = exit,
            onNegative = { showExitDialog = false },
            onNeutral = {
                checkInViewModel?.addRecord(
                    today(),
                    nowTime().toHourMinuteString(),
                    CheckInType.MASTURBATION.storeName,
                    "",
                )
                exitApp()
            },
            onPositive = { exitApp() },
            onDismiss = { showExitDialog = false },
        )
    } else if (showExitDialog) {
        ConfirmDialog(
            visible = true,
            title = confirmToExit,
            message = confirmExitMessage,
            confirmText = exit,
            dismissText = cancel,
            onConfirm = { exitApp() },
            onDismiss = { showExitDialog = false },
        )
    }

    announcement?.let { data ->
        AnnouncementDialog(
            announcementData = data,
            onDismiss = { announcement = null },
        )
    }
}
