package io.github.daisukikaffuchino.han1meviewer.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect

// 桌面端明确不做画中画：要另开一个置顶小窗、把渲染面搬过去，收益远不及成本。
// 想边看边干活直接把主窗口缩小就是了。见 DesktopPlayerHost.isInPipMode
internal actual val isPipModeSupported: Boolean = false

internal actual fun isPipPermissionGranted(): Boolean = false

internal actual fun openPipPermissionSettings() = Unit

@Composable
actual fun PlayerPipEffect(
    engine: PlaybackEngine?,
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Boolean,
) {
}
