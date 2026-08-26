package io.github.daisukikaffuchino.han1meviewer.ui.player

/**
 * iOS 是 AVPlayer。超过 2x 要看 AVPlayerItem.canPlayFastForward，不同封装/不同源
 * 结果不一样，选了不生效比没这一档更糟，所以保持在 2x——跟换内核前一致。
 */
actual val maxPlaybackSpeed: Float = 2f
