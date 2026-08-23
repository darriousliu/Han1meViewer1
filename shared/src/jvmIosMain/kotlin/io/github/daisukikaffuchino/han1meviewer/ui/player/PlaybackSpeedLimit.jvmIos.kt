package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

// composemediaplayer 自己就把倍速夹在 MAX_PLAYBACK_SPEED（2.0）以内，
// 菜单里再列 2.5 / 3.0 只会选了不生效
actual val maxPlaybackSpeed: Float = VideoPlayerState.MAX_PLAYBACK_SPEED
