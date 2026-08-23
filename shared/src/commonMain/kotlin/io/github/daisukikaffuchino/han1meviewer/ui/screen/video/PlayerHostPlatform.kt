package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect

/**
 * 播放页要用到的平台能力：窗口、屏幕方向、亮度、画中画。
 * 这些在 Android 上都挂在 Activity 上，其余平台先给空实现。
 */
interface PlayerHostPlatform {
    /**
     * 进出全屏：隐藏/恢复系统栏，并锁定方向。
     * @param preferPortrait 竖版视频进全屏时锁竖屏
     */
    fun setFullscreen(enabled: Boolean, preferPortrait: Boolean)

    /** 平台能不能由应用改屏幕亮度；不能的话亮度手势整条不出现。 */
    val supportsBrightness: Boolean

    /** 当前屏幕亮度，0..1。 */
    fun currentBrightness(): Float

    /** 覆盖窗口亮度；传 null 还原成跟随系统。 */
    fun overrideBrightness(value: Float?)

    /** 覆盖前的窗口亮度，退出全屏时用来还原；没覆盖过就是 null。 */
    fun savedBrightness(): Float?

    fun isInPipMode(): Boolean
}

@Composable
expect fun rememberPlayerHostPlatform(): PlayerHostPlatform

/** 播放期间保持常亮 + 播放页的系统栏配色，退出时还原。 */
@Composable
expect fun PlayerWindowEffect(restoreLightSystemBars: Boolean)

/**
 * 监听重力感应的方向变化（不是窗口尺寸变化），用来自动进出全屏。
 * 平板模式下调用方会传 enabled = false。
 */
@Composable
expect fun PlayerSensorOrientationEffect(enabled: Boolean, onLandscapeChange: (Boolean) -> Unit)

/**
 * 把播放页注册成画中画宿主，Activity 侧离开页面时会回调进来。
 * 不支持画中画的平台什么都不做。
 */
@Composable
expect fun PlayerPipEffect(
    shouldEnterPip: () -> Boolean,
    /** 组合里的播放状态，变化时反应式刷新画中画按钮图标。 */
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    /** 切换播放/暂停，返回切换后的播放状态，用来立刻刷新按钮，不等重组。 */
    onTogglePlayPause: () -> Boolean,
)

/** 下载通知权限；不需要申请（或平台没有）时返回 null。 */
@Composable
expect fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)?

/** 当前网络是否按流量计费。 */
expect fun isActiveNetworkMetered(): Boolean
