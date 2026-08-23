package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.github.daisukikaffuchino.han1meviewer.ui.player.ComposeMediaPlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine
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
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
    )
}
