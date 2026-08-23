package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import io.github.daisukikaffuchino.han1meviewer.ui.player.ComposeMediaPlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface

@Composable
actual fun VideoRenderSurface(
    engine: PlaybackEngine,
    modifier: Modifier,
) {
    // 预览/占位引擎不是这一种时照旧铺黑底，别让播放页整块空掉
    val mediaEngine = engine as? ComposeMediaPlaybackEngine
    if (mediaEngine == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black))
        return
    }
    VideoPlayerSurface(
        playerState = mediaEngine.player,
        // TODO: onSizeChanged 是临时诊断，桌面非全屏播放问题定位完就删。
        //  库的桌面后端每次尺寸变化都会置 isResizing，帧循环里等它落定才出帧，
        //  尺寸如果一直在抖就永远不出画面。
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { LogUtil.d("PlayerDiag", "surface=${it.width}x${it.height}") },
        contentScale = ContentScale.Fit,
    )
}
