package io.github.daisukikaffuchino.han1meviewer.ui.player

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.core.net.toUri
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.startActivitySafely
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.graphics.drawable.Icon
import android.util.Rational
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Rect
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.play_pause
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.ui.bridge.ACTION_TOGGLE_PLAY
import io.github.daisukikaffuchino.han1meviewer.ui.bridge.CurrentVideoHost
import io.github.daisukikaffuchino.han1meviewer.ui.bridge.VideoPageHost
import io.github.daisukikaffuchino.han1meviewer.ui.player.PlaybackEngine
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import kotlin.math.roundToInt

// 两个状态用不同的 requestCode：requestCode 相同的话 getBroadcast 返回的是同一个
// PendingIntent，SystemUI 会认为 RemoteAction 没变，图标不刷新（HyperOS 上实测如此）
private const val PIP_REQUEST_PLAY = 1
private const val PIP_REQUEST_PAUSE = 2

private fun Activity.pipTogglePlayIntent(isPlaying: Boolean): PendingIntent =
    PendingIntent.getBroadcast(
        this,
        if (isPlaying) PIP_REQUEST_PAUSE else PIP_REQUEST_PLAY,
        Intent(ACTION_TOGGLE_PLAY).setPackage(packageName),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

private fun Activity.pipParams(
    isPlaying: Boolean,
    extra: PictureInPictureParams.Builder.() -> Unit = {},
): PictureInPictureParams = PictureInPictureParams.Builder()
    .setAspectRatio(Rational(16, 9))
    .setActions(listOf(pipPlayPauseAction(isPlaying)))
    .apply(extra)
    .build()

private fun Activity.updatePipActions(isPlaying: Boolean) {
    if (!isInPictureInPictureMode) return
    setPictureInPictureParams(pipParams(isPlaying))
}

private fun Activity.pipPlayPauseAction(isPlaying: Boolean): RemoteAction = runBlocking {
    RemoteAction(
        Icon.createWithResource(
            this@pipPlayPauseAction,
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow,
        ),
        getString(Res.string.play_pause),
        getString(Res.string.play_pause),
        pipTogglePlayIntent(isPlaying),
    )
}

@Composable
actual fun PlayerPipEffect(
    // Activity 自己进画中画，用不到引擎
    engine: PlaybackEngine?,
    shouldEnterPip: () -> Boolean,
    isPlaying: Boolean,
    sourceBounds: () -> Rect?,
    onPipModeChanged: (Boolean) -> Unit,
    onTogglePlayPause: () -> Boolean,
) {
    val activity = LocalActivity.current ?: return
    // 全部走 rememberUpdatedState：host 只按 activity 注册一次，且内部读到的永远是最新值。
    // 之前把这些做成 DisposableEffect 的 key，host 里捕获的是旧快照，
    // 点完画中画的按钮刷新出来的还是切换前的图标。
    val currentShouldEnterPip by rememberUpdatedState(shouldEnterPip)
    val currentIsPlaying by rememberUpdatedState(isPlaying)
    val currentBounds by rememberUpdatedState(sourceBounds)
    val currentOnPipModeChanged by rememberUpdatedState(onPipModeChanged)
    val currentOnToggle by rememberUpdatedState(onTogglePlayPause)

    LaunchedEffect(activity, isPlaying) {
        activity.updatePipActions(isPlaying)
    }

    DisposableEffect(activity) {
        val host = object : VideoPageHost {
            override fun shouldEnterPip(): Boolean = currentShouldEnterPip()

            override fun enterPipMode() {
                activity.enterPictureInPictureMode(
                    activity.pipParams(currentIsPlaying) {
                        currentBounds()?.let { b ->
                            setSourceRectHint(
                                android.graphics.Rect(
                                    b.left.roundToInt(), b.top.roundToInt(),
                                    b.right.roundToInt(), b.bottom.roundToInt(),
                                )
                            )
                        }
                    }
                )
            }

            override fun onPipModeChanged(isInPip: Boolean) {
                currentOnPipModeChanged(isInPip)
                if (isInPip) activity.updatePipActions(currentIsPlaying)
            }

            override fun togglePlayPause() {
                // 引擎状态是异步回来的，这里先按返回值刷一次，
                // 上面的 LaunchedEffect 会在状态真正落地后再校正一次
                activity.updatePipActions(currentOnToggle())
            }
        }
        CurrentVideoHost.register(host)
        onDispose { CurrentVideoHost.register(null) }
    }
}

internal actual val isPipModeSupported: Boolean = true

internal actual fun isPipPermissionGranted(): Boolean {
    val appOps = applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
        Process.myUid(),
        applicationContext.packageName,
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

internal actual fun openPipPermissionSettings() {
    startActivitySafely(
        Intent(
            "android.settings.PICTURE_IN_PICTURE_SETTINGS",
            "package:${applicationContext.packageName}".toUri()
        )
    )
}
