package io.github.daisukikaffuchino.han1meviewer.ui.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCClass
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVKit.AVPictureInPictureController
import platform.CoreGraphics.CGRect
import platform.Foundation.NSCoder
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.UIKit.UIViewMeta

/**
 * 图层本身就是 AVPlayerLayer 的 UIView。
 *
 * 用 layerClass 而不是「加一个子图层」，是因为子图层不会跟着 view 自动改 frame，
 * 全屏、侧栏折叠这些尺寸变化都得自己同步一遍。
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
@ExportObjCClass
internal class PlayerUIView : UIView {

    companion object : UIViewMeta() {
        override fun layerClass(): ObjCClass = AVPlayerLayer
    }

    constructor(frame: CValue<CGRect>) : super(frame)

    constructor(coder: NSCoder) : super(coder)

    val playerLayer: AVPlayerLayer? get() = layer as? AVPlayerLayer

    var player: AVPlayer?
        get() = playerLayer?.player
        set(value) {
            playerLayer?.player = value
        }

    var videoGravity: String?
        get() = playerLayer?.videoGravity
        set(value) {
            playerLayer?.videoGravity = value
        }
}

/**
 * 画中画的全局状态。
 *
 * 画中画控制器要拿着渲染面那个 AVPlayerLayer 才能建，而建它的地方（渲染面）和用它的
 * 地方（[PlayerPipEffect]、[PlayerHostPlatform]）不在同一棵组合树上——
 * PlayerHostPlatform 更是个接口、宿主是单例，根本拿不到 playerState。所以在这里开个口子。
 */
internal object IosPipTracker {

    /**
     * 当前播放页的 AVPlayerLayer，由渲染面在进出组合时维护。
     *
     * 是 Compose State：[PlayerPipEffect] 要等图层出现了才能建控制器，
     * 普通 var 的话它不会被重新触发。
     */
    var playerLayer: AVPlayerLayer? by mutableStateOf(null)

    /** 当前的画中画控制器；没开画中画时为 null，那样系统也无从起窗。 */
    var controller: AVPictureInPictureController? = null

    /**
     * 控制器只弱引用 delegate，这里得替它留一份强引用，
     * 否则 delegate 随时可能被回收，画中画的开关事件就再也收不到了。
     */
    var delegate: NSObject? = null

    /**
     * 画中画窗口是否在展示。
     *
     * 由控制器的 delegate 维护，所以系统自己起的窗口也算得准、进后台后照样可信
     * ——这一点是换到自建控制器之后才成立的。
     */
    var isActive: Boolean = false

    /**
     * 是否已经把「进后台自动起画中画」打开了。
     *
     * 播放页据此决定退到后台时要不要按停：那个时刻画中画还没起来，[isActive] 必然是
     * false，只有「有没有武装」才是当时能用的信号。
     */
    var isAutoStartArmed: Boolean = false
}
