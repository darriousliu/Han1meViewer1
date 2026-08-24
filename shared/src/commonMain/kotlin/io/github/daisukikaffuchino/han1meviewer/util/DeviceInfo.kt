package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

/** 是否跑在 x86_64（模拟器）上。 */
expect val isX86_64Device: Boolean

/** 是否跑在桌面（JVM）平台。桌面窗口天生是大屏横向，平板模式常开且关不掉。 */
expect val isDesktopPlatform: Boolean

/** 崩溃报告头里的平台信息，每项一行，比如设备型号和系统版本。 */
expect fun crashReportPlatformInfo(): List<String>

/** 电量读数，percentage < 0 表示读不到。 */
data class BatteryStatus(
    val percentage: Int = -1,
    val isCharging: Boolean = false,
    val isFull: Boolean = false,
)

/** 读系统电量；平台读不到（桌面没有跨平台电量 API）时返回 null，整个指示器不显示。 */
@Composable
expect fun rememberBatteryStatus(): BatteryStatus?
