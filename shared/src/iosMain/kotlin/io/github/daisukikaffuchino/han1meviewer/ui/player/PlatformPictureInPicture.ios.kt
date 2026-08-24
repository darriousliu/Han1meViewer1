package io.github.daisukikaffuchino.han1meviewer.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.kdroidfilter.composemediaplayer.DefaultVideoPlayerState
import platform.AVKit.AVPictureInPictureController
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification

/**
 * 画中画交给 composemediaplayer（见库的 README_VIDEO）：`isPipEnabled` 是个能力开关，
 * 意思是「允许系统在切后台时自动起画中画」，底下还是 AVPictureInPictureController，
 * 我们不用自己再建一个。
 *
 * 所以这里只映射设置值，不像 Android 那样看 [shouldEnterPip]——Android 的
 * enterPictureInPictureMode() 是命令式的，必须在 onUserLeaveHint 当场决定「现在该不该
 * 进」；iOS 只是提前把许可交给系统，起不起由系统看内容状态定，视频没在放它自己就不起。
 *
 * 画中画窗口上的播放/暂停由系统直接驱动 AVPlayer，[onTogglePlayPause] 和
 * [sourceBounds]（起始动画由系统按图层算）在这一端都用不上。
 */
@Composable
actual fun PlayerPipEffect(
    engine: PlaybackEngine?,
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Boolean,
) {
    val mediaEngine = engine as? ComposeMediaPlaybackEngine ?: return
    val playerState = mediaEngine.player
    val appSettings by SettingsRepository.settings.collectAsStateWithLifecycle()
    // 设备不支持时也得算没开：控制器根本不会建，isPipEnabled 推过去是空转，
    // 而下面进后台那道闸门是按这个值决定摘不摘图层的，算成开就成了后台放音频。
    val autoStart = playerState.isPipSupported && appSettings.allowPipMode
    // 库的 pipController 是渲染面创建时才建的（VideoPlayerSurface 的 UIKitView
    // factory），而 isPipEnabled 的 setter 只在调用那一刻转发给它。第一次推的时候
    // 控制器多半还不存在、那次赋值就落空了，所以要在图层出现后再推一次。
    // playerLayer 是 Compose State，用它当键。
    val playerLayer = (playerState as? DefaultVideoPlayerState)?.playerLayer
    LaunchedEffect(playerState, playerLayer, autoStart) {
        playerState.isPipEnabled = autoStart
        IosPipTracker.isAutoStartArmed = autoStart
    }
    DisposableEffect(playerState) {
        // 进后台那条路要现读画中画状态，把 playerState 交给它
        IosPipTracker.playerState = playerState
        val state = playerState as? DefaultVideoPlayerState
        val center = NSNotificationCenter.defaultCenter
        val enterBackground = center.addObserverForName(
            UIApplicationDidEnterBackgroundNotification,
            UIApplication.sharedApplication,
            NSOperationQueue.mainQueue,
        ) { _ ->
            // 没开画中画还得亲手把它堵掉：canStartPictureInPictureAutomaticallyFromInline
            // 只管「内联」内容（SDK 头文件原话 embedded inline），播放页这个图层在系统
            // 眼里是主视频，进后台时 iOS 不看这个标志也会起窗。图层的 player 一置空，
            // isPictureInPicturePossible 立刻变 false，系统就起不来了。
            if (!IosPipTracker.isAutoStartArmed) state?.playerLayer?.player = null
        }
        val willEnterForeground = center.addObserverForName(
            UIApplicationWillEnterForegroundNotification,
            UIApplication.sharedApplication,
            NSOperationQueue.mainQueue,
        ) { _ ->
            state?.let { s -> s.playerLayer?.takeIf { it.player == null }?.player = s.player }
        }
        onDispose {
            center.removeObserver(enterBackground)
            center.removeObserver(willEnterForeground)
            playerState.isPipEnabled = false
            IosPipTracker.isAutoStartArmed = false
            IosPipTracker.playerState = null
        }
    }

    // isPipActive 是库维护的 Compose State，读它就能知道用户把画中画关掉了，
    // 不需要自己挂 AVPictureInPictureControllerDelegate。
    // 注意这条只在前台有效：退到后台后 Compose 不再重组，所以别处要用的画中画状态
    // 一律走 IosPipTracker 现读，不能靠这里同步。
    val pipActive = playerState.isPipActive
    LaunchedEffect(pipActive) { onPipModeChanged(pipActive) }
}

// iPad 上分屏时会返回 false
internal actual val isPipModeSupported: Boolean =
    AVPictureInPictureController.isPictureInPictureSupported()

// iOS 的画中画不需要单独授权，能不能用就看 isPipModeSupported
internal actual fun isPipPermissionGranted(): Boolean = true

internal actual fun openPipPermissionSettings() = Unit
