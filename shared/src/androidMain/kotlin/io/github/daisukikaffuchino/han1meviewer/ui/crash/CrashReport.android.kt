package io.github.daisukikaffuchino.han1meviewer.ui.crash

import android.os.Build

actual fun crashReportPlatformInfo(): List<String> = listOf(
    "Device: ${Build.MANUFACTURER} ${Build.MODEL}",
    "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
)
