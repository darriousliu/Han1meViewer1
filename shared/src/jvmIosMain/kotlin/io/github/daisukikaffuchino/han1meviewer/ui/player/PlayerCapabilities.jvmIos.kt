package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

// composemediaplayer 自己就把倍速夹在 MAX_PLAYBACK_SPEED（2.0）以内，
// 菜单里再列 2.5 / 3.0 只会选了不生效
actual val maxPlaybackSpeed: Float = VideoPlayerState.MAX_PLAYBACK_SPEED

/**
 * 这两端只有 composemediaplayer 一个内核，没有可切换的对象。
 * PlayerKernel 那三个都是 Android 专属（MediaPlayer / ExoPlayer / mpv），
 * 列出来只会让用户选了不生效，所以返回空表让设置项整个隐藏。
 */
actual val availablePlayerKernels: List<PlayerKernel> = emptyList()

// 这两端都没有 Google Cast，返回 null 让整个投屏分组不渲染
// （iOS 的投屏走 AirPlay，是另一条路径，见 CastButton.ios.kt）
actual fun googleCastAvailability(): Boolean? = null
