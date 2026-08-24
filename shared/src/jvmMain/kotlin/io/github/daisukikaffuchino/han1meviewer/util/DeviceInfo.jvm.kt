package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

actual val isX86_64Device: Boolean
    get() = false

actual val isDesktopPlatform: Boolean
    get() = true

actual fun crashReportPlatformInfo(): List<String> = listOf(
    "OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} " +
            "(${System.getProperty("os.arch")})",
    "JVM: ${System.getProperty("java.vendor")} ${System.getProperty("java.version")}",
)

// JVM 没有跨平台的电量 API（要额外引 OSHI 之类的本地库），直接不显示电量指示
@Composable
actual fun rememberBatteryStatus(): BatteryStatus? = null
