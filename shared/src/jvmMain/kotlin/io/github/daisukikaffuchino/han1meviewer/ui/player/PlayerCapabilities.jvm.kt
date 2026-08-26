package io.github.daisukikaffuchino.han1meviewer.ui.player

/**
 * 桌面是 libmpv，倍速上不封顶（mpv 自己按 speed 属性做变速不变调），
 * 跟 Android 取齐到菜单里最高那一档。
 *
 * 这里比换内核前高了一档：老的 composemediaplayer 把倍速夹在它自己的 2.0 以内，
 * 那是那个库的限制，不是桌面播放的限制。
 */
actual val maxPlaybackSpeed: Float = 3f
