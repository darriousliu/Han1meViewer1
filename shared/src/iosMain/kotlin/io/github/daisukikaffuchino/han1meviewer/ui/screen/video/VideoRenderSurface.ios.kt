package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import io.github.daisukikaffuchino.han1meviewer.ui.player.IosPipTracker
import io.github.daisukikaffuchino.han1meviewer.ui.player.MediampPlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlayerUIView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.CoreGraphics.CGRect
import platform.UIKit.UIColor

/**
 * iOS 的画面是 AVPlayerLayer。
 *
 * 这一层我们自己写、没用 mediamp-avkit-compose 自带的渲染面：画中画控制器必须拿着
 * 那个 AVPlayerLayer 才能建，而 MediaMP 的渲染面把 layer 关在 composable 里不往外给。
 * 自己持有还有个好处——能给控制器挂 delegate，画中画的开关状态就成了可信值
 * （换内核前那个库的 pipController 是 internal，系统自动起的窗口应用完全不知道）。
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoRenderSurface(
    engine: PlaybackEngine,
    modifier: Modifier,
) {
    // 预览/占位引擎不是这一种时照旧铺黑底，别让播放页整块空掉
    val player = (engine as? MediampPlaybackEngine)?.player?.impl as? AVPlayer
    if (player == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black))
        return
    }

    // 在 factory 外面建，好让下面的 DisposableEffect 拿得到同一个实例去登记/注销图层。
    val view = remember(player) {
        PlayerUIView(cValue<CGRect>()).apply {
            backgroundColor = UIColor.blackColor
            // 信箱化交给图层自己做，跟桌面 mpv 的 AspectRatioMode.FIT 对齐
            videoGravity = AVLayerVideoGravityResizeAspect
            this.player = player
        }
    }

    DisposableEffect(view) {
        IosPipTracker.playerLayer = view.playerLayer
        onDispose {
            // 先摘图层再放播放器：反过来的话系统可能在图层还挂着 player 时想起画中画
            IosPipTracker.playerLayer = null
            view.player = null
        }
    }

    UIKitView(
        factory = { view },
        modifier = modifier.fillMaxSize(),
        // 画面不吃触摸：播放页的手势（拖进度、调音量/亮度、长按倍速）全在 Compose 这一侧，
        // 让 UIView 参与命中测试只会把它们截走。
        properties = UIKitInteropProperties(interactionMode = null),
    )
}
