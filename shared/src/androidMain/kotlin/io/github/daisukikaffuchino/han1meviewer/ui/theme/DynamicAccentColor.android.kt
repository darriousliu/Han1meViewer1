package io.github.daisukikaffuchino.han1meviewer.ui.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

@Composable
actual fun dynamicAccentColorOrNull(): Color? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        colorResource(android.R.color.system_accent1_500)
    } else {
        null
    }

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual fun isDynamicColorSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
