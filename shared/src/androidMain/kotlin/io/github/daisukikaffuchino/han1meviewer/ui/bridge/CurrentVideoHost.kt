package io.github.daisukikaffuchino.han1meviewer.ui.bridge

/**
 * 当前播放页的 PiP 宿主。Activity 侧的 onUserLeaveHint / onPictureInPictureModeChanged
 * 需要拿到它，播放页自己注册，避免播放页反向依赖 MainActivity。
 */
object CurrentVideoHost {
    /** PiP 播放/暂停按钮的广播 action，Activity 侧接收、播放页侧发送。 */
    const val ACTION_TOGGLE_PLAY = "io.github.daisukikaffuchino.han1meviewer.ACTION_TOGGLE_PLAY"

    var host: VideoPageHost? = null
        private set

    fun register(host: VideoPageHost?) {
        this.host = host
    }
}
