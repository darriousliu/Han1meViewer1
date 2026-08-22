package io.github.daisukikaffuchino.han1meviewer.ui.bridge

/** 播放页与 Activity 之间的画中画桥接，只在有画中画的平台上会被注册。 */
interface VideoPageHost {
    fun shouldEnterPip(): Boolean
    fun enterPipMode()
    fun onPipModeChanged(isInPip: Boolean)
    fun togglePlayPause()
}
