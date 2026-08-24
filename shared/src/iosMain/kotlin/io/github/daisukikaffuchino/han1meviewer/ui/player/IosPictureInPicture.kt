package io.github.daisukikaffuchino.han1meviewer.ui.player

/**
 * 画中画的全局状态。
 *
 * 画中画本身交给 composemediaplayer（`isPipSupported` / `isPipEnabled` /
 * `isPipActive`，见库的 README_VIDEO），这里只留两个别处要用、又拿不到 playerState
 * 的标记：[PlayerHostPlatform.isInPipMode] 是接口方法、宿主是单例，
 * 而后台音频那条路是挂在通知上的。由 PlayerPipEffect 负责同步。
 */
internal object IosPipTracker {

    /** 画中画窗口是否在展示。 */
    var isActive: Boolean = false

    /**
     * 是否已经把「进后台自动起画中画」打开了。
     *
     * 后台音频那条路要靠它决定该不该摘图层：图层的 player 一置空，
     * isPictureInPicturePossible 立刻变 false，系统的自动画中画就根本不会启动。
     */
    var isAutoStartArmed: Boolean = false
}
