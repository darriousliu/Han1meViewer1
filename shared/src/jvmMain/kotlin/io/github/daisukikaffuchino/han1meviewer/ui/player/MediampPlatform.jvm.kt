package io.github.daisukikaffuchino.han1meviewer.ui.player

import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.mpv.MpvMediampPlayerFactory
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * 桌面三平台统一 libmpv。
 *
 * mediamp-mpv 也会用 ServiceLoader 自动注册这个工厂，但我们直接点名建，
 * 免得将来 classpath 上多一个后端时 `first()` 选到别的。
 */
internal actual fun createMediampPlayer(parentCoroutineContext: CoroutineContext): MediampPlayer {
    // 原生库由 prewarmMpvRuntime() 在启动时预热，这里只是确认它跑完了——
    // 两边同时配置运行时目录会被 mediamp 判成「重复配置」直接抛。
    awaitMpvRuntimePrewarm()
    return MpvMediampPlayerFactory().create(Unit, parentCoroutineContext)
}

/**
 * 桌面端这几项都拿不到，全部走默认值：
 * - 尺寸 mpv 自己会按 dwidth/dheight 报进 MediaProperties，不需要兜底；
 * - mpv 的 Buffering.bufferedPercentage 是「缓存填充率」，正常播放时恒为 100，
 *   不是缓冲到哪儿的比例，拿来画缓冲条只会画满，不如不画；
 * - 桌面端没有 AirPlay/Cast 一类的外部播放。
 */
internal actual fun MediampPlayer.nativeSnapshot(): NativePlaybackSnapshot =
    NativePlaybackSnapshot()

internal actual fun MediampPlayer.allowExternalPlayback() = Unit

internal actual fun localPathToUri(path: String): String = File(path).toURI().toString()
