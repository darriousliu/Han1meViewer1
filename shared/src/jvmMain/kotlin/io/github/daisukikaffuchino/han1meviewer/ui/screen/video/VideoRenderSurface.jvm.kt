package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine

// TODO(jvm): 还没有播放内核，先占位成黑底
@Composable
actual fun VideoRenderSurface(
    engine: PlaybackEngine,
    modifier: Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black))
}
