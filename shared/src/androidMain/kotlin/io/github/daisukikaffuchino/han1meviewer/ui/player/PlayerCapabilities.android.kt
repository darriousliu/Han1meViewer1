package io.github.daisukikaffuchino.han1meviewer.ui.player

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.github.daisukikaffuchino.utils.applicationContext

// ExoPlayer / MediaPlayer / mpv 都能到 3.0
actual val maxPlaybackSpeed: Float = 3f

actual val availablePlayerKernels: List<PlayerKernel> = PlayerKernel.entries

// Android 始终支持投屏，只是缺 Google Play 服务时不可用，所以只会返回 true/false
actual fun googleCastAvailability(): Boolean? =
    GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS
