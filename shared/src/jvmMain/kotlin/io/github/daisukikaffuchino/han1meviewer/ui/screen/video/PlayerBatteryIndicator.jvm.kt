package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable

// JVM 没有跨平台的电量 API（要额外引 OSHI 之类的本地库），直接不显示电量指示
@Composable
actual fun rememberBatteryStatus(): BatteryStatus? = null
