package io.github.daisukikaffuchino.han1meviewer.ui.player

import org.openani.mediamp.MediampPlayer
import kotlin.coroutines.CoroutineContext

/**
 * 外部播放（iOS 的 AirPlay）状态。
 *
 * 桌面端没有这回事，[supported] 恒为 false，播放器控件里的投屏按钮整个不出现。
 */
internal data class ExternalPlaybackStatus(
    val supported: Boolean = false,
    val active: Boolean = false,
    val deviceName: String? = null,
)

/**
 * 每个位置刷新周期读一次的原生播放器快照。
 *
 * 这几项 MediaMP 都没有跨平台的流可订阅，只能现读；打包成一次调用是为了让「每帧都要
 * 过一遍原生对象」这件事在代码里是显式的一处，实现必须够便宜（不能有 IO、不能加锁）。
 */
internal data class NativePlaybackSnapshot(
    /** 已缓冲到的位置（毫秒），0 表示不画缓冲条。 */
    val bufferedPositionMs: Long = 0L,
    /**
     * 后端在 [org.openani.mediamp.metadata.MediaProperties] 里报不出尺寸时的兜底。
     * 桌面 mpv 会报（dwidth/dheight），恒为 0；iOS 的 AVKit 后端不报，走这里。
     */
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val external: ExternalPlaybackStatus = ExternalPlaybackStatus(),
)

/**
 * 建一个当前平台的播放器：桌面是 libmpv，iOS 是 AVKit。
 *
 * 不用 MediaMP 自己的 `MediampPlayer(Unit, ctx)`——它在 JVM 上走 ServiceLoader、在 iOS 上
 * 走一个靠 `@EagerInitialization` 填的注册表，后者要求那个文件真被链进二进制。我们只按类型
 * 用一个后端，直接点名工厂更确定，也省得将来 classpath 上多一个后端时选错。
 */
internal expect fun createMediampPlayer(parentCoroutineContext: CoroutineContext): MediampPlayer

/** 见 [NativePlaybackSnapshot]。 */
internal expect fun MediampPlayer.nativeSnapshot(): NativePlaybackSnapshot

/** 换源后重新放开外部播放：AVKit 每次建 AVPlayerItem 都要重新许可，桌面端是空实现。 */
internal expect fun MediampPlayer.allowExternalPlayback()

/**
 * 把本地裸路径转成播放器认的 uri。
 *
 * 本地下载的路径在库里一律按 `.path` 存、不带 scheme，而 AVKit 后端是拿
 * `NSURL.URLWithString` 解析的，无 scheme 会得到一个相对 URL，AVURLAsset 直接失败。
 * 转义规则各平台不同（空格、中文、`#`），交给各自的标准库做，别在这里手搓百分号编码。
 */
internal expect fun localPathToUri(path: String): String

/** 没有 scheme 就是本地路径；网络源一律带 http(s):// 。 */
internal fun String.hasUriScheme(): Boolean = contains("://")
