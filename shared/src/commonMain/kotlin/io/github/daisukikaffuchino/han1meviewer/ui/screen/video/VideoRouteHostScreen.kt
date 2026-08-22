package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.produceState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.getHanimeVideoLink
import io.github.daisukikaffuchino.han1meviewer.logic.DatabaseRepo
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeEntity
import io.github.daisukikaffuchino.han1meviewer.logic.entity.WatchHistoryEntity
import io.github.daisukikaffuchino.han1meviewer.logic.exception.ParseException
import io.github.daisukikaffuchino.han1meviewer.logic.model.HanimeVideo
import io.github.daisukikaffuchino.han1meviewer.logic.model.SearchOption
import io.github.daisukikaffuchino.han1meviewer.logic.model.VideoLandscapeLayoutStyle
import io.github.daisukikaffuchino.han1meviewer.logic.state.VideoLoadingState
import io.github.daisukikaffuchino.han1meviewer.ui.component.ConfirmDialog
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.HomeRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.VideoRoute
import io.github.daisukikaffuchino.han1meviewer.ui.player.ComposePlaybackController
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngineFactory
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackPhase
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackQuality
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlayerKernel
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.CommentViewModel
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.VideoViewModel
import io.github.daisukikaffuchino.utils.loadAssetAs
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.han1meviewer.util.isX86_64Device
import io.github.daisukikaffuchino.utils.rememberCopyTextToClipboard
import io.github.daisukikaffuchino.utils.rememberShareText
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.add_to_h_keyframe
import han1meviewer.shared.generated.resources.allow
import han1meviewer.shared.generated.resources.allow_post_notification
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.confirm
import han1meviewer.shared.generated.resources.current_position_d_ms
import han1meviewer.shared.generated.resources.deny
import han1meviewer.shared.generated.resources.long_press_share_to_copy
import han1meviewer.shared.generated.resources.mobile_data_playback_warning
import han1meviewer.shared.generated.resources.no
import han1meviewer.shared.generated.resources.player_untitled_video
import han1meviewer.shared.generated.resources.reason_for_download_notification
import han1meviewer.shared.generated.resources.super_resolution_off
import han1meviewer.shared.generated.resources.super_resolution_performance
import han1meviewer.shared.generated.resources.super_resolution_quality
import han1meviewer.shared.generated.resources.sure
import han1meviewer.shared.generated.resources.sure_to_add_to_h_keyframe
import han1meviewer.shared.generated.resources.sure_to_unsubscribe
import han1meviewer.shared.generated.resources.unsubscribe_artist
import han1meviewer.shared.generated.resources.warning
import han1meviewer.shared.generated.resources.player_anime4k_label
import han1meviewer.shared.generated.resources.player_h_keyframe
import han1meviewer.shared.generated.resources.copy_to_clipboard
import han1meviewer.shared.generated.resources.fail_to_get_video_link
import han1meviewer.shared.generated.resources.large_screen_tablet_mode_hint
import han1meviewer.shared.generated.resources.msg_deny_download_notification
import han1meviewer.shared.generated.resources.ok
import han1meviewer.shared.generated.resources.pause_then_long_press
import han1meviewer.shared.generated.resources.player_keyframe_option
import han1meviewer.shared.generated.resources.video_might_not_exist
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.LocalMainBackStack
import kotlin.math.roundToInt
import io.github.daisukikaffuchino.han1meviewer.ui.player.formatPlaybackTime
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.backhandler.BackHandler
import kotlin.io.encoding.Base64
import io.github.daisukikaffuchino.han1meviewer.ui.player.SuperResolutionEngine
import io.github.daisukikaffuchino.han1meviewer.logic.exception.localizedString
import net.sergeych.sprintf.sprintf

@Suppress("DEPRECATION")
@OptIn(ExperimentalTime::class)
@Composable
fun VideoRouteHostScreen(
    route: VideoRoute,
) {
    // 窗口 / 屏幕方向 / 画中画 / 返回派发都在平台侧
    val host = rememberPlayerHostPlatform()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val copyTextToClipboard = rememberCopyTextToClipboard()
    val shareText = rememberShareText()
    val viewModel: VideoViewModel = viewModel()
    val commentViewModel: CommentViewModel = viewModel()
    val kernel = remember { PlayerKernel.fromPreference(SettingsRepository.switchPlayerKernel) }
    val playbackEngine = remember(route.videoCode, route.localUri, kernel) {
        PlaybackEngineFactory.create(
            kernel = kernel,
            allowCast = SettingsRepository.enableGoogleCast &&
                    route.localUri == null && route.videoCode != "-1",
        )
    }
    val playbackController = remember(playbackEngine) { ComposePlaybackController(playbackEngine) }
    val playbackState by playbackController.state.collectAsStateWithLifecycle()
    val appSettings by SettingsRepository.settings.collectAsStateWithLifecycle()
    val isLargeScreenDevice = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp().value >= LARGE_SCREEN_MIN_WIDTH_DP
    }
    val hostUiState by viewModel.videoHostUiStateFlow.collectAsStateWithLifecycle()
    val videoState by viewModel.hanimeVideoStateFlow.collectAsStateWithLifecycle()
    val video = viewModel.hanimeVideoFlow.collectAsStateWithLifecycle().value
    val relatedItems = video?.relatedHanimes.orEmpty()
    val restoreLightSystemBars = when (SettingsRepository.useDarkMode) {
        "always_on" -> false
        "always_off" -> true
        else -> !isSystemInDarkTheme()
    }

    LaunchedEffect(playbackController) {
        playbackController.setPlaybackSpeed(SettingsRepository.playerSpeed)
    }
    LaunchedEffect(isLargeScreenDevice) {
        val currentSettings = SettingsRepository.current
        if (
            isLargeScreenDevice &&
            !currentSettings.tabletMode &&
            !currentSettings.largeScreenTabletModeHintShown
        ) {
            SettingsRepository.update {
                it.copy(largeScreenTabletModeHintShown = true)
            }
            SonnerToast.info(Res.string.large_screen_tablet_mode_hint)
        }
    }
    val stringLongPressShare = stringResource(Res.string.long_press_share_to_copy)
    val untitledVideoText = stringResource(Res.string.player_untitled_video)
    val genres by produceState(emptyList<SearchOption>(), SettingsRepository.baseUrl) {
        value = loadAssetAs<List<SearchOption>>(
            if (SettingsRepository.baseUrl == io.github.daisukikaffuchino.han1meviewer.HanimeConstants.HANIME_URL[3]) {
                "search_options/genre_av.json"
            } else {
                "search_options/genre.json"
            }
        ).orEmpty()
    }

    var checkedQuality by remember(
        route.videoCode,
        route.localUri
    ) { mutableStateOf<String?>(null) }
    var pendingDownloadPrompt by remember(route.videoCode, route.localUri) {
        mutableStateOf<DownloadPromptState?>(null)
    }
    var videoTitle by remember(route.videoCode, route.localUri) { mutableStateOf("") }
    var isSideRelatedCollapsed by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isPlayerLocked by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1f) }
    var brightness by remember { mutableStateOf(host.currentBrightness()) }
    var speedBeforeLongPress by remember { mutableStateOf<Float?>(null) }
    var showResumeButton by remember { mutableStateOf(false) }
    var pendingPlayback by remember { mutableStateOf<PendingPlayback?>(null) }
    var mobilePlaybackConfirmed by remember(route.videoCode, route.localUri) {
        mutableStateOf(false)
    }
    var playerBounds by remember { mutableStateOf<Rect?>(null) }
    var showAddHKeyframeDialog by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var hKeyframes by remember { mutableStateOf<HKeyframeEntity?>(null) }
    var superResolutionIndex by remember { mutableStateOf(0) }
    var pendingUnsubscribeArtist by remember { mutableStateOf<HanimeVideo.Artist?>(null) }
    var showNotificationPermissionReason by remember { mutableStateOf(false) }
    val requestNotificationPermission =
        rememberRequestNotificationPermission { showNotificationPermissionReason = true }
    var showDialog by remember { mutableStateOf(false) }

    val mainBackStack = LocalMainBackStack.current
    val actions = remember(scope, viewModel, genres, mainBackStack) {
        VideoRouteActions(
            backStack = mainBackStack,
            scope = scope,
            viewModel = viewModel,
            genres = genres,
            onPendingDownloadPromptChange = { pendingDownloadPrompt = it },
            getCheckedQuality = { checkedQuality },
            setCheckedQuality = { checkedQuality = it },
            onOpenUri = uriHandler::openUri,
            onCopyText = copyTextToClipboard,
            onRequestUnsubscribe = { pendingUnsubscribeArtist = it },
            onRequestNotificationPermission = { requestNotificationPermission?.invoke() },
        )
    }

    fun exitFullscreen() {
        if (!isFullscreen) return
        isFullscreen = false
        host.setFullscreen(enabled = false, preferPortrait = false)
        brightness = host.currentBrightness()
    }

    fun enterFullscreen(forceLandscape: Boolean = false) {
        isFullscreen = true
        val engineState = playbackController.state.value.engine
        host.setFullscreen(
            enabled = true,
            preferPortrait = !forceLandscape &&
                    engineState.videoWidth > 0 &&
                    engineState.videoHeight > engineState.videoWidth,
        )
    }

    BackHandler(enabled = isFullscreen) { exitFullscreen() }

    PlayerPipEffect(
        shouldEnterPip = {
            val state = playbackController.state.value.engine
            !state.isCasting &&
                    state.phase == PlaybackPhase.Ready &&
                    (state.isPlaying || state.positionMs > 0L)
        },
        isPlaying = playbackState.engine.isPlaying,
        sourceBounds = { playerBounds },
        onPipModeChanged = viewModel::setPipMode,
        onTogglePlayPause = {
            playbackController.togglePlayPause()
            playbackController.state.value.engine.isPlaying
        },
    )

    PlayerWindowEffect(restoreLightSystemBars = restoreLightSystemBars)

    DisposableEffect(playbackController) {
        onDispose {
            playbackController.release()
            exitFullscreen()
        }
    }

    PlayerSensorOrientationEffect(enabled = !appSettings.tabletMode) { isLandscape ->
        if (isLandscape && !isFullscreen) {
            enterFullscreen(forceLandscape = true)
        } else if (!isLandscape && isFullscreen) {
            exitFullscreen()
        }
    }

    DisposableEffect(lifecycleOwner, playbackController, route.videoCode) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (route.videoCode != "-1") {
                        val progress = playbackController.state.value.engine.positionMs
                        scope.launch {
                            DatabaseRepo.WatchHistory.updateProgress(
                                route.videoCode,
                                progress
                            )
                        }
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    if (!host.isInPipMode() &&
                        !playbackController.state.value.engine.isCasting
                    ) {
                        playbackController.pause()
                        exitFullscreen()
                    }
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycleOwner.lifecycle.removeObserver(lifecycleObserver) }
    }

    LaunchedEffect(route.videoCode, route.localUri) {
        checkedQuality = null
        pendingDownloadPrompt = null
        videoTitle = ""
        viewModel.videoCode = route.videoCode
        viewModel.fromDownload = route.videoCode == "-1" || route.localUri != null
        viewModel.getHanimeVideo(route.videoCode, route.localUri)
    }

    LaunchedEffect(route.videoCode, route.localUri, playbackController, viewModel.fromDownload) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.hanimeVideoStateFlow.collect { state ->
                when (state) {
                    is VideoLoadingState.Error -> {
                        SonnerToast.error(state.throwable.localizedString())
                        if (state.throwable is ParseException) {
                            uriHandler.openUri(getHanimeVideoLink(route.videoCode))
                        }
                    }

                    is VideoLoadingState.Loading -> Unit

                    is VideoLoadingState.Success -> {
                        val info = state.info
                        videoTitle = info.title
                        val qualities = info.videoUrls.map { (label, link) ->
                            PlaybackQuality(
                                label = label,
                                uri = link.link,
                                mimeType = link.subtype?.let { "video/$it" },
                            )
                        }
                        if (qualities.isEmpty()) {
                            SonnerToast.error(Res.string.fail_to_get_video_link)
                            uriHandler.openUri(getHanimeVideoLink(route.videoCode))
                        } else {
                            val history = DatabaseRepo.WatchHistory.findBy(route.videoCode)
                            showResumeButton = SettingsRepository.allowResumePlayback &&
                                    (history?.progress ?: 0L) > 5_000L
                            val request = PendingPlayback(
                                title = info.title,
                                qualities = qualities,
                                preferredQuality = SettingsRepository.videoQuality,
                                artworkUri = info.coverUrl,
                                startPositionMs = history?.progress ?: 0L,
                            )
                            if (!viewModel.fromDownload &&
                                !SettingsRepository.disableMobileDataWarning &&
                                !mobilePlaybackConfirmed &&
                                isActiveNetworkMetered()
                            ) {
                                pendingPlayback = request
                            } else {
                                playbackController.load(
                                    title = request.title,
                                    qualities = request.qualities,
                                    preferredQuality = request.preferredQuality,
                                    artworkUri = request.artworkUri,
                                    startPositionMs = request.startPositionMs,
                                    playWhenReady = true,
                                )
                            }
                        }
                        if (!viewModel.fromDownload) {
                            viewModel.insertWatchHistoryWithCover(
                                WatchHistoryEntity(
                                    info.coverUrl,
                                    info.title,
                                    info.uploadTimeMillis,
                                    kotlin.time.Clock.System.now().toEpochMilliseconds(),
                                    route.videoCode,
                                )
                            )
                        }
                    }

                    is VideoLoadingState.NoContent -> SonnerToast.error(Res.string.video_might_not_exist)
                }
            }
        }
    }

    LaunchedEffect(viewModel, route.videoCode) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.loadDownloadedFlow.collect { entity ->
                val newQuality = checkedQuality ?: return@collect
                pendingDownloadPrompt = DownloadPromptState(
                    newQuality = newQuality,
                    oldQuality = entity?.quality,
                    oldGroupId = entity?.groupId,
                )
            }
        }
    }

    LaunchedEffect(route.videoCode) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
            viewModel.observeKeyframe(route.videoCode).collect {
                hKeyframes = it
                viewModel.hKeyframes = it
            }
        }
    }

    LaunchedEffect(playbackState.engine.isPlaying) {
        viewModel.setScrollDisabled(playbackState.engine.isPlaying)
    }

    LaunchedEffect(showResumeButton) {
        if (showResumeButton) {
            kotlinx.coroutines.delay(5_000L.milliseconds)
            showResumeButton = false
        }
    }

    val countdownLabel = remember(playbackState.engine.positionMs, hKeyframes, isFullscreen) {
        if (!isFullscreen || !SettingsRepository.hKeyframesEnable) {
            null
        } else {
            hKeyframes?.keyframes.orEmpty().mapIndexedNotNull { index, keyframe ->
                val remaining = keyframe.position - playbackState.engine.positionMs
                if (remaining in 0L until SettingsRepository.whenCountdownRemind) {
                    val seconds = remaining / 1000L
                    val time = if (seconds >= 1L) {
                        (seconds + 1L).toString()
                    } else {
                        "%.1f".sprintf(remaining / 1000f)
                    }
                    if (SettingsRepository.showCommentWhenCountdown && !keyframe.prompt.isNullOrBlank()) {
                        "#${index + 1} ${keyframe.prompt}\n$time"
                    } else {
                        time
                    }
                } else {
                    null
                }
            }.firstOrNull()
        }
    }

    val resolvedPlayerHeightDp = when {
        hostUiState.isInPipMode -> null
        appSettings.tabletMode -> if (isSideRelatedCollapsed) 500.dp else 400.dp
        else -> 250.dp
    }

    LaunchedEffect(resolvedPlayerHeightDp, hostUiState.playerHeightDp) {
        if (hostUiState.playerHeightDp != resolvedPlayerHeightDp) {
            viewModel.setPlayerHeightDp(resolvedPlayerHeightDp)
        }
    }

    VideoShellContent(
        isTabletMode = appSettings.tabletMode,
        isInPipMode = hostUiState.isInPipMode,
        isFullscreen = isFullscreen,
        playerHeightDp = resolvedPlayerHeightDp,
        playbackEngine = playbackEngine,
        posterUrl = video?.coverUrl,
        title = videoTitle,
        currentTime = formatPlaybackTime(playbackState.engine.positionMs),
        totalTime = formatPlaybackTime(playbackState.engine.durationMs),
        progress = playbackProgress(
            playbackState.engine.positionMs,
            playbackState.engine.durationMs
        ),
        bufferedProgress = playbackProgress(
            playbackState.engine.bufferedPositionMs,
            playbackState.engine.durationMs,
        ),
        currentVolume = volume,
        currentBrightness = brightness,
        isPlaying = playbackState.engine.isPlaying,
        isPlaybackEnded = playbackState.engine.phase == PlaybackPhase.Ended,
        showCastButton = playbackState.engine.isCastSupported,
        isCasting = playbackState.engine.isCasting,
        castDeviceName = playbackState.engine.castDeviceName,
        isLocked = isPlayerLocked,
        showPoster = !playbackState.engine.hasRenderedFirstFrame,
        showLoading =
            videoState is VideoLoadingState.Loading ||
                    playbackState.engine.phase == PlaybackPhase.Preparing,
        showRetry = playbackState.engine.phase == PlaybackPhase.Error,
        showResumeButton = showResumeButton,
        onPlayClick = playbackController::togglePlayPause,
        onReplay = playbackController::replay,
        onBackClick = host::dispatchBack,
        onHomeClick = {
            mainBackStack.popTo(HomeRoute)
        },
        onFullscreenClick = {
            if (isFullscreen) exitFullscreen() else enterFullscreen()
        },
        onLockClick = { isPlayerLocked = !isPlayerLocked },
        onProgressChange = { value ->
            val duration = playbackState.engine.durationMs
            if (duration > 0L) playbackController.seekTo((duration * value).toLong())
        },
        onRetry = {
            video?.let { info ->
                val qualities =
                    info.videoUrls.map { (label, link) ->
                        PlaybackQuality(label, link.link, mimeType = link.subtype?.let { "video/$it" })
                    }
                playbackController.load(
                    title = info.title,
                    qualities = qualities,
                    preferredQuality = SettingsRepository.videoQuality,
                    artworkUri = info.coverUrl,
                )
            }
        },
        onResumeClick = {
            playbackController.seekTo(0L)
            showResumeButton = false
        },
        qualities = playbackState.qualities,
        selectedQuality = playbackState.qualities
            .getOrNull(playbackState.selectedQualityIndex)
            ?.label,
        onQualitySelected = playbackController::selectQuality,
        playbackSpeed = playbackState.engine.playbackSpeed,
        onPlaybackSpeedSelected = playbackController::setPlaybackSpeed,
        superResolutionLabel = stringResource(Res.string.player_anime4k_label),
        superResolutionOptions = if (kernel == PlayerKernel.MpvPlayer && !playbackState.engine.isCasting) {
            listOf(
                stringResource(Res.string.super_resolution_off),
                stringResource(Res.string.super_resolution_performance),
                stringResource(Res.string.super_resolution_quality),
            )
        } else {
            emptyList()
        },
        selectedSuperResolutionIndex = superResolutionIndex,
        onSuperResolutionSelected = { index ->
            superResolutionIndex = index
            (playbackEngine as? SuperResolutionEngine)?.setSuperResolution(index)
        },
        hKeyframeLabel = stringResource(Res.string.player_h_keyframe),
        isHKeyframesEnabled = SettingsRepository.hKeyframesEnable,
        hKeyframeOptions = hKeyframes?.keyframes.orEmpty().mapIndexed { index, keyframe ->
            stringResource(
                Res.string.player_keyframe_option,
                index + 1,
                formatPlaybackTime(keyframe.position),
            )
        },
        hKeyframes = hKeyframes?.keyframes.orEmpty(),
        isHKeyframeLocal = hKeyframes?.author == null,
        onHKeyframeSelected = { index ->
            hKeyframes?.keyframes?.getOrNull(index)?.position?.let(playbackController::seekTo)
        },
        onHKeyframeUpdated = { oldKeyframe, newKeyframe ->
            viewModel.modifyHKeyframe(route.videoCode, oldKeyframe, newKeyframe)
        },
        onHKeyframeDeleted = { keyframe ->
            viewModel.removeHKeyframe(route.videoCode, keyframe)
        },
        onHKeyframeLongPress = {
            if (playbackState.engine.isPlaying) {
                SonnerToast.info(Res.string.pause_then_long_press)
            } else {
                showAddHKeyframeDialog =
                    playbackState.engine.positionMs to videoTitle.ifBlank { untitledVideoText }
            }
        },
        onLongPressStart = {
            if (playbackState.engine.isPlaying) {
                val currentSpeed = playbackState.engine.playbackSpeed
                speedBeforeLongPress = currentSpeed
                playbackController.setPlaybackSpeed(
                    (currentSpeed * SettingsRepository.longPressSpeedTime).coerceAtMost(5f)
                )
            }
        },
        onLongPressEnd = {
            speedBeforeLongPress?.let(playbackController::setPlaybackSpeed)
            speedBeforeLongPress = null
        },
        onVolumeChange = { value ->
            volume = value
            playbackController.setVolume(value)
        },
        onBrightnessChange = { value ->
            brightness = value
            host.overrideBrightness(value.coerceIn(0.01f, 1f))
        },
        onProgressGesture = { value ->
            val duration = playbackState.engine.durationMs
            if (duration > 0L) playbackController.seekTo((duration * value).toLong())
        },
        progressGestureSensitivity = realProgressSensitivity(SettingsRepository.slideSensitivity),
        countdownLabel = countdownLabel,
        videoAspectRatio = if (
            playbackState.engine.videoWidth > 0 &&
            playbackState.engine.videoHeight > 0
        ) {
            playbackState.engine.videoWidth.toFloat() / playbackState.engine.videoHeight.toFloat()
        } else {
            16f / 9f
        },
        onPlayerBoundsChanged = { playerBounds = it },
        tabsContent = {
            VideoRouteContent(
                videoCode = route.videoCode,
                videoState = videoState,
                videoViewModel = viewModel,
                commentViewModel = commentViewModel,
                fromDownload = viewModel.fromDownload,
                pendingDownloadPrompt = pendingDownloadPrompt,
                onPendingDownloadPromptChange = { pendingDownloadPrompt = it },
                onRetry = { viewModel.getHanimeVideo(route.videoCode, route.localUri) },
                onOpenVideo = { item -> mainBackStack.add(VideoRoute(item.videoCode)) },
                onOpenArtist = actions::openArtistSearch,
                onNavigateToSearch = actions::openTagSearch,
                onToggleSubscribe = actions::toggleArtistSubscription,
                onToggleFavorite = actions::toggleFavorite,
                onRateVideo = actions::rateVideo,
                onManageMyList = actions::updateMyListSelection,
                onQuickCheckIn = actions::quickCheckIn,
                onPrepareDownload = { quality, item ->
                    checkedQuality = quality
                    item?.let(actions::startDownloadFlow)
                },
                onConfirmDownloadPrompt = { item, autoCreateGroup ->
                    item?.let {
                        actions.confirmPendingDownload(
                            it,
                            pendingDownloadPrompt,
                            autoCreateGroup,
                        )
                    }
                },
                onRequestOpenOfficialDownloadPage = actions::openOfficialDownloadPage,
                onOpenWebPage = actions::openVideoWebPage,
                onOpenOriginalComic = actions::openOriginalComic,
                onOpenShare = shareText,
                onCopyText = {
                    copyTextToClipboard(it)
                    SonnerToast.success(Res.string.copy_to_clipboard)
                },
                onIntroductionLinkClick = actions::openIntroductionLink,
                stringLongPressShare = stringLongPressShare,
                onCommentCountChange = viewModel::setCommentBadgeCount,
            )
        },
        classicTabletLayout = if (
            appSettings.tabletMode &&
            appSettings.videoLandscapeLayoutStyle == VideoLandscapeLayoutStyle.Classic
        ) {
            ClassicTabletLayoutConfig(
                relatedItems = relatedItems,
                onHideRelatedInIntroChange = { viewModel.hideRelatedInIntro = it },
                onSideRelatedCollapsedChange = { isSideRelatedCollapsed = it },
                onOpenVideo = { item -> mainBackStack.add(VideoRoute(item.videoCode)) },
            )
        } else {
            null
        },
        modifier = Modifier.fillMaxSize(),
    )

    showAddHKeyframeDialog?.let { (currentPosition, title) ->
        ConfirmDialog(
            visible = true,
            title = stringResource(Res.string.add_to_h_keyframe),
            message = buildString {
                appendLine(stringResource(Res.string.sure_to_add_to_h_keyframe))
                append(stringResource(Res.string.current_position_d_ms, currentPosition))
            },
            confirmText = stringResource(Res.string.confirm),
            dismissText = stringResource(Res.string.cancel),
            onConfirm = {
                viewModel.appendHKeyframe(
                    route.videoCode,
                    title,
                    HKeyframeEntity.Keyframe(position = currentPosition, prompt = null),
                )
                showAddHKeyframeDialog = null
            },
            onDismiss = { showAddHKeyframeDialog = null },
        )
    }

    pendingUnsubscribeArtist?.let { artist ->
        ConfirmDialog(
            visible = true,
            title = stringResource(Res.string.unsubscribe_artist),
            message = stringResource(Res.string.sure_to_unsubscribe),
            confirmText = stringResource(Res.string.sure),
            dismissText = stringResource(Res.string.no),
            onConfirm = {
                actions.confirmUnsubscribe(artist)
                pendingUnsubscribeArtist = null
            },
            onDismiss = { pendingUnsubscribeArtist = null },
        )
    }

    ConfirmDialog(
        visible = showNotificationPermissionReason,
        title = stringResource(Res.string.allow_post_notification),
        message = stringResource(Res.string.reason_for_download_notification),
        confirmText = stringResource(Res.string.allow),
        dismissText = stringResource(Res.string.deny),
        onConfirm = {
            showNotificationPermissionReason = false
            requestNotificationPermission?.invoke()
        },
        onDismiss = {
            showNotificationPermissionReason = false
            SonnerToast.warning(Res.string.msg_deny_download_notification)
        },
    )

    ConfirmDialog(
        visible = pendingPlayback != null,
        title = stringResource(Res.string.warning),
        message = stringResource(Res.string.mobile_data_playback_warning),
        confirmText = stringResource(Res.string.confirm),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = {
            val request = pendingPlayback
            pendingPlayback = null
            mobilePlaybackConfirmed = true
            request?.let {
                playbackController.load(
                    title = it.title,
                    qualities = it.qualities,
                    preferredQuality = it.preferredQuality,
                    artworkUri = it.artworkUri,
                    startPositionMs = it.startPositionMs,
                    playWhenReady = true,
                )
            }
        },
        onDismiss = { pendingPlayback = null },
    )

    if (showDialog) {
        Base64Dialog(onDismiss = { showDialog = false })
    }

    LaunchedEffect(Unit) {
        if (!isX86_64Device) {
            val isFailed = signatureCheckResult() == Base64.decode("ZmFpbGVk").decodeToString()

            when {
                isFailed -> SonnerToast.error(
                    Base64.decode("5qCh6aqM5bSp5rqD77yM6K+35ZCR5byA5Y+R6ICF5Y+N6aaI").decodeToString()
                )

                else -> showDialog = !BuildConfig.DEBUG && !isSignatureValid()
            }
        }
    }
}

@Composable
fun Base64Dialog(
    onDismiss: () -> Unit
) {
    val decodedTitle = remember {
        Base64.decode("562+5ZCN5qCh6aqM5aSx6LSl").decodeToString()
    }
    val decodedContent = remember {
        Base64.decode("5L2g5LiL6L295Yiw5LqG6KKr56+h5pS555qE5bqU55So44CC5pys5bqU55So5byA5rqQ5YWN6LS55peg5bm/5ZGK77yM5Lil56aB5aKZ5YaF5byV5rWB44CB5pCs6L+Q44CB5YCS5Y2W44CC5aaC5p6c5L2g6K6k5Li66L+Z5piv6K+v5oql77yM6K+35ZCR5byA5Y+R6ICF5Y+N6aaI44CC").decodeToString()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = decodedTitle) },
        text = { Text(text = decodedContent) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.ok))
            }
        }
    )
}

private fun playbackProgress(positionMs: Long, durationMs: Long): Float =
    if (durationMs <= 0L) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)



private data class PendingPlayback(
    val title: String,
    val qualities: List<PlaybackQuality>,
    val preferredQuality: String?,
    val artworkUri: String?,
    val startPositionMs: Long,
)


private fun realProgressSensitivity(value: Int): Float {
    val clampedValue = value.coerceIn(1, 7)
    return 4f - (clampedValue - 1) * (3.5f / 6f)
}

private const val LARGE_SCREEN_MIN_WIDTH_DP = 600
