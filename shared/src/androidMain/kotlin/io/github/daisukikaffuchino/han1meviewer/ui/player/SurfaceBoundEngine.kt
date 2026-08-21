package io.github.daisukikaffuchino.han1meviewer.ui.player

import android.view.Surface

/**
 * 需要绑定 Android Surface 的播放内核。surface 是平台概念，
 * 不放进公共的 [PlaybackEngine]，由 VideoRenderSurface 的 Android 实现来用。
 */
interface SurfaceBoundEngine {
    fun attachSurface(surface: Surface)
    fun detachSurface(surface: Surface)
}
