package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

// 桌面端没有 AirPlay/Cast 一类的外部播放
internal actual fun VideoPlayerState.externalPlaybackStatus() = ExternalPlaybackStatus()

internal actual fun VideoPlayerState.allowExternalPlayback() = Unit

// 桌面端的后端没有暴露缓冲区间，缓冲条不画
internal actual fun VideoPlayerState.bufferedPositionMs(): Long = 0L
