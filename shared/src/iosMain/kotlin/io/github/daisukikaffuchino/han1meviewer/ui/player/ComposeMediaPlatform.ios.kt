package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.DefaultVideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionPortAirPlay
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.currentRoute
import platform.AVFoundation.allowsExternalPlayback
import platform.AVFoundation.externalPlaybackActive

/**
 * AirPlay 状态直接读 AVPlayer 的属性，没另挂 KVO：播放状态本来就被库里 15fps 的
 * 进度回调带着刷，下一帧就会把变化带出来，不值得再加一层观察者。代价是暂停时
 * 打开 AirPlay 要等下次播放才反映出来。
 */
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
