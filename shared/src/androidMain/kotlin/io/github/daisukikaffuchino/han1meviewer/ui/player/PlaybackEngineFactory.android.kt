package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.daisukikaffuchino.utils.applicationContext

actual object PlaybackEngineFactory {
    actual fun create(kernel: PlayerKernel, allowCast: Boolean): PlaybackEngine {
        val context = applicationContext
        val localEngine = when (kernel) {
            PlayerKernel.MediaPlayer -> SystemPlaybackEngine(context)
            PlayerKernel.ExoPlayer -> ExoPlaybackEngine(context)
            PlayerKernel.MpvPlayer -> MpvPlaybackEngine(context)
        }
        return if (allowCast) {
            CastPlaybackEngine.createOrLocal(context, localEngine)
        } else {
            localEngine
        }
    }
}
