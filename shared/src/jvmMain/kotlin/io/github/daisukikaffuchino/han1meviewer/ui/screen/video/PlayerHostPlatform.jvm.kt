package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.window.WindowPlacement
import io.github.daisukikaffuchino.han1meviewer.ui.window.LocalDesktopWindow

private class DesktopPlayerHost(private val window: ComposeWindow?) : PlayerHostPlatform {

    /** 退出全屏要还原成进全屏之前的摆放方式，不能一律回 Floating。 */
    private var placementBeforeFullscreen: WindowPlacement? = null

    override fun setFullscreen(enabled: Boolean, preferPortrait: Boolean) {
        val window = window ?: return
        if (enabled) {
            if (placementBeforeFullscreen == null) {
                placementBeforeFullscreen = window.placement
            }
            window.placement = WindowPlacement.Fullscreen
        } else {
            window.placement = placementBeforeFullscreen ?: WindowPlacement.Floating
            placementBeforeFullscreen = null
        }
    }

    // 桌面没有「应用改屏幕亮度」这回事
    override fun currentBrightness(): Float = 1f
    override fun overrideBrightness(value: Float?) = Unit
    override fun savedBrightness(): Float? = null

    // TODO(jvm): 桌面画中画要独立窗口，等播放内核补上再说
    override fun isInPipMode(): Boolean = false
}

@Composable
actual fun rememberPlayerHostPlatform(): PlayerHostPlatform {
    val window = LocalDesktopWindow.current
    return remember(window) { DesktopPlayerHost(window) }
}

@Composable
actual fun PlayerWindowEffect(restoreLightSystemBars: Boolean) {
}

// 桌面没有重力感应
@Composable
actual fun PlayerSensorOrientationEffect(
    enabled: Boolean,
    onLandscapeChange: (Boolean) -> Unit,
) {
}

// TODO(jvm): 画中画,同上
@Composable
actual fun PlayerPipEffect(
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Boolean,
) {
}

// 没有需要动态申请的通知权限
@Composable
actual fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)? = null

// 桌面走的是以太网/Wi-Fi，没有「计费网络」这个概念，恒为 false 就是正确语义
actual fun isActiveNetworkMetered(): Boolean = false
