package io.github.daisukikaffuchino.han1meviewer.ui.player

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.avkit.AVKitMediampPlayerFactory
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionModeMoviePlayback
import platform.AVFAudio.AVAudioSessionPortAirPlay
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.currentRoute
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.CMTimeRangeValue
import platform.AVFoundation.allowsExternalPlayback
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.externalPlaybackActive
import platform.AVFoundation.loadedTimeRanges
import platform.AVFoundation.presentationSize
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSURL
import platform.Foundation.NSValue
import kotlin.coroutines.CoroutineContext

internal actual fun createMediampPlayer(parentCoroutineContext: CoroutineContext): MediampPlayer {
    configurePlaybackAudioSession()
    return AVKitMediampPlayerFactory().create(Unit, parentCoroutineContext)
}

/**
 * 音频会话必须是 playback：默认的 soloAmbient 会被静音开关掐掉声音，也不允许进后台，
 * 画中画整套（连同 Info.plist 里的 UIBackgroundModes=audio）都建立在这个类别上。
 *
 * MediaMP 的 AVKit 后端不碰音频会话，这是我们自己的责任——换内核前是
 * composemediaplayer 在建播放器时顺手配的，别以为它会自己回来。
 */
@OptIn(ExperimentalForeignApi::class)
private fun configurePlaybackAudioSession() {
    val session = AVAudioSession.sharedInstance()
    session.setCategory(
        AVAudioSessionCategoryPlayback,
        mode = AVAudioSessionModeMoviePlayback,
        options = 0u,
        error = null,
    )
    session.setActive(true, error = null)
}

/** MediaMP 把底下的 AVPlayer 原样暴露在 impl 上，几项它没抽象的东西直接从这儿读。 */
private val MediampPlayer.avPlayer: AVPlayer? get() = impl as? AVPlayer

/**
 * AirPlay 状态、缓冲位置和画面尺寸都直接读 AVPlayer，没另挂 KVO：这些值本来就被
 * 位置刷新带着走，下一拍就会把变化带出来，不值得再加一层观察者。代价是暂停时
 * 打开 AirPlay 要等下次播放才反映出来。
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun MediampPlayer.nativeSnapshot(): NativePlaybackSnapshot {
    val player = avPlayer ?: return NativePlaybackSnapshot(
        external = ExternalPlaybackStatus(supported = true),
    )
    val externalActive = player.externalPlaybackActive
    val external = ExternalPlaybackStatus(
        supported = true,
        active = externalActive,
        deviceName = if (externalActive) airPlayRouteName() else null,
    )
    // item 没 ready 就去读 presentationSize / duration / loadedTimeRanges 会抛 ObjC 异常，
    // 那是直接 abort、连崩溃处理器都进不去，一定要先看 status。
    val item = player.currentItem?.takeIf { it.status == AVPlayerItemStatusReadyToPlay }
        ?: return NativePlaybackSnapshot(external = external)
    // AVKit 后端不往 MediaProperties 里报画面尺寸，只能自己从 item 上取。
    // presentationSize 已经把 preferredTransform 算进去了，竖屏视频不会拿到躺倒的尺寸。
    val size = item.presentationSize.useContents { width.toInt() to height.toInt() }
    return NativePlaybackSnapshot(
        bufferedPositionMs = item.bufferedPositionMs(),
        videoWidth = size.first,
        videoHeight = size.second,
        external = external,
    )
}

/**
 * 从 AVPlayerItem 的 loadedTimeRanges 算已缓冲位置。
 *
 * 拖动后这些区间会是断开的，所以只认「包含当前播放点」的那一段——取全局最大值的话，
 * 跳回前面已经缓冲过的地方时会画出一条根本不连着的缓冲条。都不包含就报 0 不画。
 */
@OptIn(ExperimentalForeignApi::class)
private fun AVPlayerItem.bufferedPositionMs(): Long {
    val nowSec = CMTimeGetSeconds(currentTime())
    if (nowSec.isNaN()) return 0L
    var end = 0.0
    loadedTimeRanges.forEach { value ->
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

/** 投屏时音频路由也切到了 Apple TV，设备名从那儿拿。 */
private fun airPlayRouteName(): String? =
    AVAudioSession.sharedInstance().currentRoute.outputs
        .filterIsInstance<AVAudioSessionPortDescription>()
        .firstOrNull { it.portType == AVAudioSessionPortAirPlay }
        ?.portName

/** AVKit 后端每次 open 都新建 AVPlayerItem，AirPlay 许可挂在 AVPlayer 上，换源后重新放开。 */
internal actual fun MediampPlayer.allowExternalPlayback() {
    avPlayer?.allowsExternalPlayback = true
}

/**
 * AVKit 后端是拿 `NSURL.URLWithString` 解析 uri 的，裸路径会得到一个没有 scheme 的
 * 相对 URL，AVURLAsset 直接失败；fileURLWithPath 会把空格、中文、`#` 一并转义好。
 */
internal actual fun localPathToUri(path: String): String =
    NSURL.fileURLWithPath(path).absoluteString ?: path
