package io.github.daisukikaffuchino.han1meviewer.ui.player

import kotlinx.coroutines.flow.MutableStateFlow

/** 按用户选的内核创建播放引擎。 */
expect object PlaybackEngineFactory {
    fun create(kernel: PlayerKernel, allowCast: Boolean = true): PlaybackEngine
}

/** 支持超分的播放内核（目前只有 mpv）。 */
interface SuperResolutionEngine {
    fun setSuperResolution(index: Int)
}

/** 平台还没有播放内核时的空引擎，页面照常渲染，只是永远不出画面。 */
class NoopPlaybackEngine : PlaybackEngine {
    override val state = MutableStateFlow(PlaybackEngineState())

    override fun load(request: PlaybackRequest) = Unit
    override fun play() = Unit
    override fun pause() = Unit
    override fun seekTo(positionMs: Long) = Unit
    override fun setPlaybackSpeed(speed: Float) = Unit
    override fun setVolume(volume: Float) = Unit
    override fun release() = Unit
}
