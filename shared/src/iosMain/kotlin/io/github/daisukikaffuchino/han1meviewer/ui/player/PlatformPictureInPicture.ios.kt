package io.github.daisukikaffuchino.han1meviewer.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import kotlinx.cinterop.BetaInteropApi
import platform.AVKit.AVPictureInPictureController
import platform.AVKit.AVPictureInPictureControllerDelegateProtocol
import platform.darwin.NSObject

/**
 * 画中画由我们自己建 AVPictureInPictureController。
 *
 * 关键点是「没开画中画就不建控制器」：AVKit 里控制器是画中画唯一的入口，不建它系统就
 * 起不了窗，进后台时 iOS 会照常把带画面的播放按停——正好是我们想要的「不在后台放」。
 * 换内核前那个库无条件建控制器，才需要在进后台时把图层的 player 摘掉去堵它。
 *
 * 这里也不像 Android 那样看 [shouldEnterPip]：Android 的 enterPictureInPictureMode()
 * 是命令式的，必须在 onUserLeaveHint 当场决定「现在该不该进」；iOS 只是提前把许可交给
 * 系统，起不起由系统看内容状态定，视频没在放它自己就不起。
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
    val appSettings by SettingsRepository.settings.collectAsStateWithLifecycle()
    val currentOnPipModeChanged by rememberUpdatedState(onPipModeChanged)
    // 设备不支持时也得算没开：控制器根本建不出来，而进后台那道闸门是按这个值
    // 决定要不要按停播放的，算成开就成了后台放音频。
    val autoStart = isPipModeSupported && appSettings.allowPipMode
    // 图层由渲染面创建，第一次组合时多半还不存在，要等它出现再建控制器
    val playerLayer = IosPipTracker.playerLayer

    DisposableEffect(playerLayer, autoStart) {
        if (playerLayer == null || !autoStart) {
            IosPipTracker.isAutoStartArmed = false
            return@DisposableEffect onDispose { }
        }
        val delegate = PipDelegate { active ->
            IosPipTracker.isActive = active
            currentOnPipModeChanged(active)
        }
        val controller = AVPictureInPictureController(playerLayer = playerLayer)
        controller.delegate = delegate
        controller.canStartPictureInPictureAutomaticallyFromInline = true
        IosPipTracker.controller = controller
        IosPipTracker.delegate = delegate
        IosPipTracker.isAutoStartArmed = true
        onDispose {
            controller.canStartPictureInPictureAutomaticallyFromInline = false
            controller.delegate = null
            IosPipTracker.controller = null
            IosPipTracker.delegate = null
            IosPipTracker.isAutoStartArmed = false
            IosPipTracker.isActive = false
        }
    }
}

/**
 * 只认开始/结束这两个事件。
 *
 * 没实现 restoreUserInterfaceForPictureInPictureStop：我们的播放页在画中画期间一直留在
 * 组合树里，系统把窗口收掉后画面自己就回到内联图层了，不需要额外还原界面。
 */
@OptIn(BetaInteropApi::class)
private class PipDelegate(
    private val onChanged: (Boolean) -> Unit,
) : NSObject(), AVPictureInPictureControllerDelegateProtocol {

    override fun pictureInPictureControllerDidStartPictureInPicture(
        pictureInPictureController: AVPictureInPictureController,
    ) {
        onChanged(true)
    }

    override fun pictureInPictureControllerDidStopPictureInPicture(
        pictureInPictureController: AVPictureInPictureController,
    ) {
        onChanged(false)
    }
}

// iPad 上分屏时会返回 false
internal actual val isPipModeSupported: Boolean =
    AVPictureInPictureController.isPictureInPictureSupported()

// iOS 的画中画不需要单独授权，能不能用就看 isPipModeSupported
internal actual fun isPipPermissionGranted(): Boolean = true

internal actual fun openPipPermissionSettings() = Unit
