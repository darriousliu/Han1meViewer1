package io.github.daisukikaffuchino.han1meviewer.ui.bridge

/**
 * 画中画播放/暂停按钮的广播 action：播放页侧发送（PendingIntent），
 * MainActivity 侧接收。Intent 是 Android 概念，不放进公共的 [CurrentVideoHost]。
 */
const val ACTION_TOGGLE_PLAY = "io.github.daisukikaffuchino.han1meviewer.ACTION_TOGGLE_PLAY"
