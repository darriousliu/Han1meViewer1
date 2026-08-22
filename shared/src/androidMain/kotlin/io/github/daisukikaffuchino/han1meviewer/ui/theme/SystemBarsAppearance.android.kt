package io.github.daisukikaffuchino.han1meviewer.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat

@Composable
actual fun ApplySystemBarsAppearance(isDark: Boolean, windowBackground: Color) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        view.context.findActivity()?.window?.let { window ->
            window.setBackgroundDrawable(windowBackground.toArgb().toDrawable())
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
