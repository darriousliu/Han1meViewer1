package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

// 桌面端没有 AirPlay/Cast 一类的外部播放
internal actual fun VideoPlayerState.externalPlaybackStatus() = ExternalPlaybackStatus()

internal actual fun VideoPlayerState.allowExternalPlayback() = Unit

// 桌面端窗口不在前台也照放，没有要装的东西
internal actual fun VideoPlayerState.installBackgroundPlayback(): (() -> Unit)? = null
