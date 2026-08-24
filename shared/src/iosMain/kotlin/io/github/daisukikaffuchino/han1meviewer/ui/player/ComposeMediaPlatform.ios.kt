package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.DefaultVideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionPortAirPlay
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.currentRoute
import platform.AVFoundation.CMTimeRangeValue
import platform.AVFoundation.allowsExternalPlayback
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.externalPlaybackActive
import platform.AVFoundation.loadedTimeRanges
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSValue

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

/**
 * 从 AVPlayerItem 的 loadedTimeRanges 算已缓冲位置。
 *
 * 拖动后这些区间会是断开的，所以只认「包含当前播放点」的那一段——取全局最大值的话，
 * 跳回前面已经缓冲过的地方时会画出一条根本不连着的缓冲条。都不包含就报 0 不画。
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun VideoPlayerState.bufferedPositionMs(): Long {
    val item = (this as? DefaultVideoPlayerState)?.player?.currentItem ?: return 0L
    val nowSec = CMTimeGetSeconds(item.currentTime())
    if (nowSec.isNaN()) return 0L
    var end = 0.0
    item.loadedTimeRanges.forEach { value ->
        (value as? NSValue)?.CMTimeRangeValue?.useContents {
            val startSec = CMTimeGetSeconds(start.readValue())
            val endSec = startSec + CMTimeGetSeconds(duration.readValue())
            // 容一点余量：刚 seek 完 currentTime 可能比区间起点早那么一点
            if (!startSec.isNaN() && !endSec.isNaN() &&
                startSec <= nowSec + 0.5 && endSec >= nowSec && endSec > end
            ) {
                end = endSec
            }
        }
    }
    return if (end <= 0.0) 0L else (end * 1000).toLong()
}
