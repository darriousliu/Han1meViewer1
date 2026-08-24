package io.github.daisukikaffuchino.han1meviewer.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

@Stable
inline val WindowAdaptiveInfo.isWidthCompact: Boolean
    get() = windowSizeClass.isWidthCompact

@Stable
inline val WindowAdaptiveInfo.isWidthAtLeastMedium: Boolean
    get() = windowSizeClass.isWidthAtLeastMedium

@Stable
inline val WindowAdaptiveInfo.isWidthAtLeastExpanded: Boolean
    get() = windowSizeClass.isWidthAtLeastExpanded

@Stable
inline val WindowAdaptiveInfo.isHeightCompact: Boolean
    get() = windowSizeClass.isHeightCompact

@Stable
inline val WindowAdaptiveInfo.isHeightAtLeastMedium: Boolean
    get() = windowSizeClass.isHeightAtLeastMedium


@Stable
inline val WindowSizeClass.isWidthCompact: Boolean
    get() = !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

@Stable
inline val WindowSizeClass.isWidthAtLeastMedium: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

@Stable
inline val WindowSizeClass.isWidthAtLeastExpanded: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

@Stable
inline val WindowSizeClass.isHeightCompact: Boolean
    get() = !isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

@Stable
inline val WindowSizeClass.isHeightAtLeastMedium: Boolean
    get() = isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

@Stable
inline val WindowSizeClass.isHeightAtLeastExpanded: Boolean
    get() = isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)

private val zeroInsets = WindowInsets(0.dp) // single instance to be shared

@Stable
val WindowInsets.Companion.Zero: WindowInsets
    get() = zeroInsets