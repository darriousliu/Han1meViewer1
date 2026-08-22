package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect

// TODO(jvm): 全屏、亮度、画中画都还没实现
private object NoopPlayerHost : PlayerHostPlatform {
    override fun setFullscreen(enabled: Boolean, preferPortrait: Boolean) = Unit
    override fun currentBrightness(): Float = 1f
    override fun overrideBrightness(value: Float?) = Unit
    override fun savedBrightness(): Float? = null
    override fun isInPipMode(): Boolean = false
    override fun dispatchBack() = Unit
}

@Composable
actual fun rememberPlayerHostPlatform(): PlayerHostPlatform = NoopPlayerHost

@Composable
actual fun PlayerWindowEffect(restoreLightSystemBars: Boolean) {
}

@Composable
actual fun PlayerSensorOrientationEffect(
    enabled: Boolean,
    onLandscapeChange: (Boolean) -> Unit,
) {
}

@Composable
actual fun PlayerPipEffect(
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
) {
}

// 没有需要动态申请的通知权限
@Composable
actual fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)? = null

// TODO(jvm): 计费网络判断
actual fun isActiveNetworkMetered(): Boolean = false
