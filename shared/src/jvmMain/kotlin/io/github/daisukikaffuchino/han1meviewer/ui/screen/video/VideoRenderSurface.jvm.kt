package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.daisukikaffuchino.han1meviewer.ui.player.MediampPlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine
import org.openani.mediamp.mpv.MpvMediampPlayer
import org.openani.mediamp.mpv.compose.MpvMediampPlayerSurface

/**
 * 桌面端的画面走 mpv 自己的渲染面：原生渲染线程把帧写进 GPU 纹理环
 * （macOS Metal/IOSurface、Windows D3D11 共享句柄、Linux GLX），
 * 再由 Skiko 直接采样，全程不过 CPU。
 *
 * 画面的信箱化（ContentScale.Fit 那种效果）由 mpv 侧按 AspectRatioMode.FIT 自己做，
 * 我们只负责给它一个盒子。
 */
@Composable
actual fun VideoRenderSurface(
    engine: PlaybackEngine,
    modifier: Modifier,
) {
    // 预览/占位引擎不是这一种时照旧铺黑底，别让播放页整块空掉
    val player = (engine as? MediampPlaybackEngine)?.player as? MpvMediampPlayer
    if (player == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black))
        return
    }
    MpvMediampPlayerSurface(player, modifier = modifier.fillMaxSize())
}
