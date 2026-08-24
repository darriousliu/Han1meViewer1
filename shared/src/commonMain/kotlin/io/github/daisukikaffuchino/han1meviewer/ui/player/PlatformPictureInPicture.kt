package io.github.daisukikaffuchino.han1meviewer.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect

/** 这个平台有没有画中画；没有的话设置项整个不渲染，别留个点了没反应的开关。 */
internal expect val isPipModeSupported: Boolean

internal expect fun isPipPermissionGranted(): Boolean

internal expect fun openPipPermissionSettings()

/**
 * 把播放页注册成画中画宿主，Activity 侧离开页面时会回调进来。
 * 不支持画中画的平台什么都不做。
 *
 * @param engine 当前播放引擎。iOS 的画中画要拿引擎底下的 AVPlayerLayer 建控制器，
 *   Android 由 Activity 自己进画中画，用不到它。
 */
@Composable
expect fun PlayerPipEffect(
    engine: PlaybackEngine?,
    shouldEnterPip: () -> Boolean,
    /** 组合里的播放状态，变化时反应式刷新画中画按钮图标。 */
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    /** 切换播放/暂停，返回切换后的播放状态，用来立刻刷新按钮，不等重组。 */
    onTogglePlayPause: () -> Boolean,
)
