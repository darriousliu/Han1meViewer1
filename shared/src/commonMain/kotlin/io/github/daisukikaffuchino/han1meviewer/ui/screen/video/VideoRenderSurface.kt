package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine

/**
 * 视频渲染表面。播放页里只有这一块是平台强相关的——画面要交给平台自己的
 * 渲染载体（Android SurfaceView / iOS AVPlayerLayer / 桌面 …），
 * 其余控制层、手势、面板都是共用 UI。
 */
@Composable
expect fun VideoRenderSurface(
    engine: PlaybackEngine,
    modifier: Modifier = Modifier,
)
