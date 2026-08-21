package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.daisukikaffuchino.han1meviewer.ui.player.MpvPlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine
import io.github.daisukikaffuchino.han1meviewer.ui.player.SurfaceBoundEngine

@Composable
actual fun VideoRenderSurface(
    engine: PlaybackEngine,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        (engine as? SurfaceBoundEngine)?.attachSurface(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int,
                    ) {
                        // mpv 换 surface 尺寸必须显式告知，否则画面不刷新
                        (engine as? MpvPlaybackEngine)?.updateSurfaceSize(width, height)
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        (engine as? SurfaceBoundEngine)?.detachSurface(holder.surface)
                    }
                })
            }
        }
    )
}
