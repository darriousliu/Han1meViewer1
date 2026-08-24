package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.DefaultVideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionPortAirPlay
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.currentRoute
import platform.AVFoundation.allowsExternalPlayback
import platform.AVFoundation.externalPlaybackActive
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

/**
 * AirPlay 状态直接读 AVPlayer 的属性，没另挂 KVO：播放状态本来就被库里 15fps 的
 * 进度回调带着刷，下一帧就会把变化带出来，不值得再加一层观察者。代价是暂停时
 * 打开 AirPlay 要等下次播放才反映出来。
 */
/** 武装了自动画中画时，留给系统起窗口的宽限时间。 */
private const val PIP_START_GRACE_SECONDS = 1L

internal actual fun VideoPlayerState.externalPlaybackStatus(): ExternalPlaybackStatus {
    val player = (this as? DefaultVideoPlayerState)?.player
        ?: return ExternalPlaybackStatus(supported = true)
    val active = player.externalPlaybackActive
    return ExternalPlaybackStatus(
        supported = true,
        active = active,
        deviceName = if (active) airPlayRouteName() else null,
    )
}

/** 投屏时音频路由也切到了 Apple TV，设备名从那儿拿。 */
private fun airPlayRouteName(): String? =
    AVAudioSession.sharedInstance().currentRoute.outputs
        .filterIsInstance<AVAudioSessionPortDescription>()
        .firstOrNull { it.portType == AVAudioSessionPortAirPlay }
        ?.portName

/** 库建 AVPlayer 时写死了 allowsExternalPlayback = false，每次换源都要重新放开。 */
internal actual fun VideoPlayerState.allowExternalPlayback() {
    (this as? DefaultVideoPlayerState)?.player?.allowsExternalPlayback = true
}

internal actual fun VideoPlayerState.installBackgroundPlayback(): (() -> Unit)? {
    val state = this as? DefaultVideoPlayerState ?: return null
    val center = NSNotificationCenter.defaultCenter

    fun reattachLayer() {
        val layer = state.playerLayer ?: return
        if (layer.player == null) layer.player = state.player
    }

    // AVPlayerLayer 还挂着播放器的话，系统一进后台就把播放停了——带画面的播放不给后台跑。
    // 摘掉图层只剩音频，配上 Info.plist 的 UIBackgroundModes=audio 才能接着放。
    //
    // 但不能无条件立刻摘：图层的 player 一置空，isPictureInPicturePossible 立刻变
    // false，系统的自动画中画就根本不会启动，willStart 也永远不来——原来靠
    // onWillStartPip 补救的那条路等不到，表现就是进后台完全不出画中画。
    // 所以按「有没有武装自动画中画」分开处理。
    val didEnterBackground = center.addObserverForName(
        UIApplicationDidEnterBackgroundNotification,
        null,
        NSOperationQueue.mainQueue,
    ) { _ ->
        when {
            // 已经在画中画：图层必须留着，否则窗口是黑的
            IosPipTracker.isActive -> Unit
            // 没武装（设置关了 / 平台不支持）：立刻摘，走纯后台音频
            !IosPipTracker.isAutoStartArmed -> state.playerLayer?.player = null
            // 武装了：给系统一点时间去起画中画，起不来再退回纯音频。
            // 注意这条回落路径上 iOS 可能已经把播放暂停了，只能保证音频链路是通的。
            else -> dispatch_after(
                dispatch_time(DISPATCH_TIME_NOW, (PIP_START_GRACE_SECONDS * NSEC_PER_SEC.toLong())),
                dispatch_get_main_queue(),
            ) {
                if (!IosPipTracker.isActive) state.playerLayer?.player = null
            }
        }
    }
    val willEnterForeground = center.addObserverForName(
        UIApplicationWillEnterForegroundNotification,
        null,
        NSOperationQueue.mainQueue,
    ) { _ ->
        reattachLayer()
    }
    return {
        center.removeObserver(didEnterBackground)
        center.removeObserver(willEnterForeground)
    }
}
