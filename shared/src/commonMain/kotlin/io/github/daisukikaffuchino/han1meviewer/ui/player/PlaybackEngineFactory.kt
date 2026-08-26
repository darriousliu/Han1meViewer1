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

/**
 * 不出画面的空引擎，页面照常渲染。
 *
 * 两种用法：平台还没有播放内核时用默认状态；内核压根建不起来时传一个
 * [PlaybackPhase.Error] 的初始状态，播放页会照常给出错误与重试入口。
 */
class NoopPlaybackEngine(
    initialState: PlaybackEngineState = PlaybackEngineState(),
) : PlaybackEngine {
    override val state = MutableStateFlow(initialState)

    override fun load(request: PlaybackRequest) = Unit
    override fun play() = Unit
    override fun pause() = Unit
    override fun seekTo(positionMs: Long) = Unit
    override fun setPlaybackSpeed(speed: Float) = Unit
    override fun setVolume(volume: Float) = Unit
    override fun release() = Unit
}
