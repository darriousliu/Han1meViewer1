package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.AVKit.AVRoutePickerView
import platform.UIKit.UIColor

/**
 * iOS 的投屏就是 AirPlay，没有 Google Cast。
 *
 * 挑设备交给系统的 AVRoutePickerView，画面能不能投出去取决于 AVPlayer 的
 * allowsExternalPlayback——见 ComposeMediaPlatform.ios.kt 里的 allowExternalPlayback()。
 */
@Composable
actual fun CastButton(modifier: Modifier) {
    UIKitView(
        factory = {
            AVRoutePickerView().apply {
                // 播放器控件是黑底，默认的黑色图标在上面看不见
                tintColor = UIColor.whiteColor
                activeTintColor = UIColor.whiteColor
                backgroundColor = UIColor.clearColor
                // 优先列 Apple TV 这类能收视频的设备，而不是蓝牙耳机
                prioritizesVideoDevices = true
            }
        },
        modifier = modifier,
    )
}
