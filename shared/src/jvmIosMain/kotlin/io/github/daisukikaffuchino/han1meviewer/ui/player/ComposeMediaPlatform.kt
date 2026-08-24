package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

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

/** 当前的外部播放状态；实现要足够便宜，播放状态每帧都会读它。 */
internal expect fun VideoPlayerState.externalPlaybackStatus(): ExternalPlaybackStatus

/** 换源后重新放开外部播放：composemediaplayer 每次建 AVPlayer 都把它关掉。 */
internal expect fun VideoPlayerState.allowExternalPlayback()

/**
 * 已缓冲到的位置（毫秒），0 表示不画缓冲条。
 *
 * composemediaplayer 没暴露这个，只能各端自己从底下的播放器要；桌面端的后端拿不到，
 * 恒返回 0。跟 [externalPlaybackStatus] 一样，播放状态每帧都会读它，实现要够便宜。
 */
internal expect fun VideoPlayerState.bufferedPositionMs(): Long
