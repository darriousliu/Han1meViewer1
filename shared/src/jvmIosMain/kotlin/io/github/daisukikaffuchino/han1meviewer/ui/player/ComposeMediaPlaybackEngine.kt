package io.github.daisukikaffuchino.han1meviewer.ui.player

import androidx.compose.runtime.snapshotFlow
import io.github.kdroidfilter.composemediaplayer.InitialPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.createVideoPlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 桌面与 iOS 共用的播放内核，底下是 composemediaplayer
 * （桌面按操作系统选原生后端，iOS 是 AVPlayer）。
 *
 * 它只有一个后端、不提供内核切换，所以在我们的模型里就是「一个内核」；
 * Android 那三个内核（含 mpv 超分）仍由 androidMain 的 actual 自己管。
 */
internal class ComposeMediaPlaybackEngine : PlaybackEngine {

    /** 渲染面要用同一个 state，所以暴露出去给 VideoRenderSurface。 */
    val player: VideoPlayerState = createVideoPlayerState()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableStateFlow(PlaybackEngineState())
    override val state: StateFlow<PlaybackEngineState> = _state.asStateFlow()

    private var ended = false
    /** openUri 之后 duration 才知道，起播位置只能等 duration 出来再补 seek。 */
    private var pendingStartMs = 0L

    init {
        player.onPlaybackEnded = { ended = true }
        // 库的状态是 Compose State，用 snapshotFlow 桥到 StateFlow
        scope.launch {
            snapshotFlow { readState() }.collect { snapshot ->
                _state.value = snapshot
                if (pendingStartMs > 0L && snapshot.durationMs > 0L) {
                    val target = pendingStartMs
                    pendingStartMs = 0L
                    seekTo(target)
                }
            }
        }
    }

    private fun readState(): PlaybackEngineState {
        val error = player.error
        val metadata = player.metadata
        val durationMs = (player.duration * 1000).toLong()
        // sliderPos 是 Compose State，currentTime / duration 在部分平台是算出来的
        // 普通 getter——snapshotFlow 只观察真正读到的 State，只读后者的话播放中根本
        // 不会再触发（表现就是进度条不动，一暂停才跳一下）。位置以 sliderPos 为准。
        val progressPerMille = player.sliderPos
        return PlaybackEngineState(
            phase = when {
                error != null -> PlaybackPhase.Error
                !player.hasMedia -> PlaybackPhase.Idle
                ended -> PlaybackPhase.Ended
                player.isLoading -> PlaybackPhase.Preparing
                else -> PlaybackPhase.Ready
            },
            isPlaying = player.isPlaying,
            isBuffering = player.hasMedia && player.isLoading,
            positionMs = if (durationMs > 0L) {
                (durationMs * (progressPerMille / 1000.0)).toLong()
            } else {
                (player.currentTime * 1000).toLong()
            },
            durationMs = durationMs,
            // 库没暴露缓冲进度，先按 0 报，UI 上就是不画缓冲条
            bufferedPositionMs = 0L,
            playbackSpeed = player.playbackSpeed,
            videoWidth = metadata.width ?: 0,
            videoHeight = metadata.height ?: 0,
            hasRenderedFirstFrame = player.hasMedia && !player.isLoading,
            errorMessage = error?.toString(),
        )
    }

    override fun load(request: PlaybackRequest) {
        ended = false
        pendingStartMs = request.startPositionMs
        player.clearError()
        player.loop = request.looping
        // TODO: openUri 不收请求头，需要 Referer / UA 的源在这两端会取不到
        player.openUri(
            request.uri,
            if (request.playWhenReady) InitialPlayerState.PLAY else InitialPlayerState.PAUSE,
        )
    }

    override fun play() = player.play()

    override fun pause() = player.pause()

    /** 库的 seekTo 收的是 0..1000 的千分比，不是毫秒。 */
    override fun seekTo(positionMs: Long) {
        val durationMs = (player.duration * 1000).toLong()
        if (durationMs <= 0L) {
            pendingStartMs = positionMs
            return
        }
        player.seekTo((positionMs.toFloat() / durationMs * 1000f).coerceIn(0f, 1000f))
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.playbackSpeed = speed
    }

    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    override fun release() {
        scope.cancel()
        player.dispose()
    }
}
