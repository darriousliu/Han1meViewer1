package io.github.daisukikaffuchino.han1meviewer.ui.player

// TODO(ios): 还没有播放内核
actual object PlaybackEngineFactory {
    actual fun create(kernel: PlayerKernel, allowCast: Boolean): PlaybackEngine =
        NoopPlaybackEngine()
}
