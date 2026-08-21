package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable

// TODO(jvm): 读系统电量，暂时返回未知
@Composable
actual fun rememberBatteryStatus(): BatteryStatus = BatteryStatus()
