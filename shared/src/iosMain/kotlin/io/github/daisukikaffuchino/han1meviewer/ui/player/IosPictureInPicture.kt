package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

/**
 * 画中画的全局状态。
 *
 * 画中画本身交给 composemediaplayer（`isPipSupported` / `isPipEnabled` /
 * `isPipActive`，见库的 README_VIDEO），这里只是给拿不到 playerState 的地方
 * 开个口子：[PlayerHostPlatform.isInPipMode] 是接口方法、宿主是单例，
 * 而后台音频那条路是挂在系统通知上的。
 */
internal object IosPipTracker {

    /** 当前播放页的 playerState，由 PlayerPipEffect 维护。 */
    var playerState: VideoPlayerState? = null

    /**
     * 画中画窗口是否在展示。
     *
     * **自动画中画时这个值不可信**：库的 isPipActive 只在主动调 enterPip() 时置位，
     * 系统自己起的窗口它完全不知道，而它的 pipController 是 internal、挂不上 delegate。
     * 所以后台那条路不要拿它做判断，只能用来看「我们主动进过画中画没有」。
     */
    val isActive: Boolean get() = playerState?.isPipActive == true

    /**
     * 是否已经把「进后台自动起画中画」打开了。
     *
     * 后台音频那条路要靠它决定该不该摘图层：图层的 player 一置空，
     * isPictureInPicturePossible 立刻变 false，系统的自动画中画就根本不会启动。
     */
    var isAutoStartArmed: Boolean = false
}
