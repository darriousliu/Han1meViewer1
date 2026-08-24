package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

/**
 * 画中画的全局状态。
 *
 * 画中画本身交给 composemediaplayer（`isPipSupported` / `isPipEnabled` /
 * `isPipActive`，见库的 README_VIDEO），这里只是给拿不到 playerState 的地方开个口子
 * —— [io.github.daisukikaffuchino.han1meviewer.ui.player.PlayerHostPlatform]
 * 是接口、宿主是单例。
 */
internal object IosPipTracker {

    /** 当前播放页的 playerState，由 PlayerPipEffect 维护。 */
    var playerState: VideoPlayerState? = null

    /**
     * 画中画窗口是否在展示。
     *
     * **自动画中画时这个值不可信**：库的 isPipActive 只在主动调 enterPip() 时置位，
     * 系统自己起的窗口它完全不知道，而它的 pipController 是 internal、挂不上 delegate。
     * 需要判断「进后台后该不该继续播」时用 [isAutoStartArmed]，别用这个。
     */
    val isActive: Boolean get() = playerState?.isPipActive == true

    /**
     * 是否已经把「进后台自动起画中画」打开了。
     *
     * 播放页据此决定退到后台时要不要按停：不能用 [isActive]，理由见上。
     */
    var isAutoStartArmed: Boolean = false
}
