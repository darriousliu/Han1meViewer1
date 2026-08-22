package io.github.daisukikaffuchino.han1meviewer.ui.bridge

/**
 * 当前播放页的画中画宿主。播放页自己注册，平台壳（Android 是 MainActivity 的
 * onUserLeaveHint / onPictureInPictureModeChanged）从这里取，
 * 避免播放页反向依赖平台壳。
 *
 * iOS、macOS、Windows 也有画中画，所以注册表放公共层，各平台壳照样能用。
 */
object CurrentVideoHost {
    var host: VideoPageHost? = null
        private set

    fun register(host: VideoPageHost?) {
        this.host = host
    }
}
