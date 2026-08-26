package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.daisukikaffuchino.utils.LogUtil

/**
 * 桌面与 iOS 只有一个内核，kernel 参数用不上；
 * 投屏两端都没有（isGoogleCastAvailable 已按平台返回 null），allowCast 同理。
 */
actual object PlaybackEngineFactory {
    actual fun create(kernel: PlayerKernel, allowCast: Boolean): PlaybackEngine = try {
        MediampPlaybackEngine()
    } catch (e: Throwable) {
        // 桌面的 libmpv 是运行时从 runtime jar 里解出来再加载的：装的包缺当前架构、
        // Windows 少 VC++ 运行库这类问题会在建播放器时当场抛，而且是 UnsatisfiedLinkError
        // 这种 Error。这里是在播放页的 remember 里、属于组合期，抛出去就是整个应用崩。
        // 退化成一个恒为 Error 的空引擎，播放页照常渲染并给出重试入口。
        LogUtil.e("PlaybackEngineFactory", "播放内核初始化失败", e)
        NoopPlaybackEngine(
            PlaybackEngineState(
                phase = PlaybackPhase.Error,
                errorMessage = e.message ?: e.toString(),
            ),
        )
    }
}
