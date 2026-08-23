package io.github.daisukikaffuchino.han1meviewer.ui.player

/**
 * 桌面与 iOS 只有一个内核，kernel 参数用不上；
 * 投屏两端都没有（isGoogleCastAvailable 已按平台返回 null），allowCast 同理。
 */
actual object PlaybackEngineFactory {
    actual fun create(kernel: PlayerKernel, allowCast: Boolean): PlaybackEngine =
        ComposeMediaPlaybackEngine()
}
