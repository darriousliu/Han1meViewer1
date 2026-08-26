package io.github.daisukikaffuchino.han1meviewer.ui.player

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openani.mediamp.MediaStatus
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackEvent
import org.openani.mediamp.errorOrNull
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.features.PlaybackSpeed
import org.openani.mediamp.isLoadingOrBuffering
import org.openani.mediamp.source.UriMediaData

/**
 * 桌面与 iOS 共用的播放内核，底下是 MediaMP
 * （桌面三平台统一 libmpv，iOS 是 AVKit/AVPlayer）。
 *
 * 两端的后端不同，但控制层是 MediaMP 的同一套 commonMain API，所以引擎只写这一份；
 * 真正分平台的只剩 [MediampPlatform] 里那几个 expect（建播放器、现读原生快照）
 * 和渲染面。它只有一个后端、不提供内核切换，所以在我们的模型里就是「一个内核」；
 * Android 那三个内核（含 mpv 超分）仍由 androidMain 的 actual 自己管。
 */
internal class MediampPlaybackEngine : PlaybackEngine {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * 渲染面要用同一个 player，所以暴露出去给 VideoRenderSurface。
     *
     * 生命周期挂在 [scope] 上：MediaMP 会盯着父 Job，Job 结束时自动 close()，
     * 所以就算调用方漏了 [release]，页面销毁也不会漏掉原生播放器。
     */
    val player: MediampPlayer = createMediampPlayer(scope.coroutineContext)

    private val speedFeature = player.features[PlaybackSpeed.Key]
    private val audioFeature = player.features[AudioLevelController.Key]

    private val mutableState = MutableStateFlow(PlaybackEngineState())
    override val state: StateFlow<PlaybackEngineState> = mutableState.asStateFlow()

    /**
     * 首帧是一次性闩锁，跟 Android 的 ExoPlaybackEngine 同语义：起播出画后就一直为真，
     * 只有下一次 load 才重置。不能用「当前是否不在加载」——seek 时会短暂回到 loading，
     * 那样封面会被盖回画面上，表现就是拖进度时画面和封面一闪一闪，还把手势指示器挡了。
     */
    private var renderedFirstFrame = false

    /**
     * 视频尺寸只认这一次 load 里第一次拿到的值。
     *
     * 布局拿它算宽高比、宽高比又反过来决定渲染面大小，所以尺寸每抖一次渲染面就要重新
     * 布局一次。后端报的是媒体真实分辨率（mpv 的 dwidth/dheight、AVKit 的
     * presentationSize），本身不会抖；闩住只是为了不让「换源过程中短暂归零」那一下
     * 传导成一次多余的 resize——桌面的 resize 是要重建 GPU 纹理环的，能省则省。
     */
    private var latchedVideoWidth = 0
    private var latchedVideoHeight = 0

    /** MediaMP 没有循环播放开关，自己在播完事件里接一下。 */
    private var looping = false

    /** setMediaData 是 suspend 的，换源要能打断上一次还没开完的。 */
    private var loadJob: Job? = null

    init {
        scope.launch {
            player.state.collect { playerState ->
                if (playerState.mediaStatus == MediaStatus.Ready) renderedFirstFrame = true
                mutableState.update {
                    it.copy(
                        phase = when (playerState.mediaStatus) {
                            MediaStatus.Idle, MediaStatus.Released -> PlaybackPhase.Idle
                            MediaStatus.Opening -> PlaybackPhase.Preparing
                            MediaStatus.Ready -> PlaybackPhase.Ready
                            MediaStatus.Ended -> PlaybackPhase.Ended
                            is MediaStatus.Error -> PlaybackPhase.Error
                        },
                        isPlaying = playerState.isPlaying,
                        isBuffering = playerState.isLoadingOrBuffering,
                        hasRenderedFirstFrame = renderedFirstFrame,
                        errorMessage = playerState.errorOrNull
                            ?.let { error -> error.message ?: error.toString() },
                    )
                }
            }
        }

        // 位置是刷得最勤的一条，顺手把「只能现读、没有流」的那几项一起带出来
        scope.launch {
            player.currentPositionMillis.collect { positionMs ->
                val snapshot = player.nativeSnapshot()
                latchVideoSize(snapshot.videoWidth, snapshot.videoHeight)
                mutableState.update {
                    it.copy(
                        positionMs = positionMs,
                        bufferedPositionMs = snapshot.bufferedPositionMs,
                        videoWidth = latchedVideoWidth,
                        videoHeight = latchedVideoHeight,
                        isCastSupported = snapshot.external.supported,
                        isCasting = snapshot.external.active,
                        castDeviceName = snapshot.external.deviceName,
                    )
                }
            }
        }

        scope.launch {
            player.mediaProperties.collect { properties ->
                latchVideoSize(properties?.videoWidth ?: 0, properties?.videoHeight ?: 0)
                mutableState.update {
                    it.copy(
                        durationMs = properties?.durationMillis ?: 0L,
                        videoWidth = latchedVideoWidth,
                        videoHeight = latchedVideoHeight,
                    )
                }
            }
        }

        speedFeature?.let { speed ->
            scope.launch {
                speed.valueFlow.collect { value ->
                    mutableState.update { it.copy(playbackSpeed = value) }
                }
            }
        }

        scope.launch {
            player.events.collect { event ->
                if (event is PlaybackEvent.MediaEnded && looping) {
                    player.seekTo(0L)
                    player.play()
                }
            }
        }
    }

    private fun latchVideoSize(width: Int, height: Int) {
        if (latchedVideoWidth == 0 && width > 0 && height > 0) {
            latchedVideoWidth = width
            latchedVideoHeight = height
        }
    }

    override fun load(request: PlaybackRequest) {
        loadJob?.cancel()
        renderedFirstFrame = false
        latchedVideoWidth = 0
        latchedVideoHeight = 0
        looping = request.looping
        // 当场把状态推到「准备中」，跟 Android 的 ExoPlaybackEngine 一样。
        // setMediaData 是 suspend 的，不这么做的话切集/换清晰度后、状态机第一次发流之前，
        // 进度条还挂着上一集的时长和位置，封面也不会盖回来。
        mutableState.update {
            it.copy(
                phase = PlaybackPhase.Preparing,
                isPlaying = false,
                isBuffering = true,
                positionMs = request.startPositionMs,
                durationMs = 0L,
                bufferedPositionMs = 0L,
                videoWidth = 0,
                videoHeight = 0,
                hasRenderedFirstFrame = false,
                errorMessage = null,
            )
        }
        val uri = if (request.uri.hasUriScheme()) request.uri else localPathToUri(request.uri)
        loadJob = scope.launch {
            try {
                // 起播位置交给 MediaMP：它在 open 阶段就把位置消化掉了，
                // 不像老的库要等 duration 出来再补一次 seek（会看到从 0 跳过去）。
                player.setMediaData(
                    UriMediaData(uri, request.headers),
                    playWhenReady = request.playWhenReady,
                    startPositionMillis = request.startPositionMs,
                )
                player.allowExternalPlayback()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // 打开失败状态机已经报进 player.state（MediaStatus.Error）、由上面那条
                // collect 转成 PlaybackPhase.Error 了，这里只是别让协程带着异常终止。
            }
        }
    }

    override fun play() = player.play()

    override fun pause() = player.pause()

    /** MediaMP 的 seekTo 收的就是毫秒。 */
    override fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    override fun setPlaybackSpeed(speed: Float) {
        speedFeature?.set(speed)
    }

    override fun setVolume(volume: Float) {
        // maxVolume 各后端不同（mpv 是 2.0 = 200%，AVPlayer 是 1.0），
        // 我们这一层的 1.0 就是「原音量」，直接传即可，别按 maxVolume 缩放。
        audioFeature?.setVolume(volume.coerceIn(0f, 1f))
    }

    override fun release() {
        // 先停掉可能还在 open 的那次，再关播放器，最后才收协程：
        // 反过来先 cancel scope 的话，close 里那些要在状态机线程上跑的收尾就没人跑了。
        loadJob?.cancel()
        player.close()
        scope.cancel()
    }
}
