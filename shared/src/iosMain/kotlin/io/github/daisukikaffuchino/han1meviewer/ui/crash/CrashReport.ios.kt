package io.github.daisukikaffuchino.han1meviewer.ui.crash

import platform.UIKit.UIDevice

actual fun crashReportPlatformInfo(): List<String> = listOf(
    "Device: ${UIDevice.currentDevice.model}",
    "iOS: ${UIDevice.currentDevice.systemVersion}",
)
