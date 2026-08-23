package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryLevelDidChangeNotification
import platform.UIKit.UIDeviceBatteryState
import platform.UIKit.UIDeviceBatteryStateDidChangeNotification
import kotlin.math.roundToInt

@Composable
actual fun rememberBatteryStatus(): BatteryStatus? {
    var status by remember { mutableStateOf(BatteryStatus()) }

    DisposableEffect(Unit) {
        val device = UIDevice.currentDevice
        // 不开监控 batteryLevel 恒为 -1；模拟器上即使开了也读不到，会显示问号图标
        val wasEnabled = device.batteryMonitoringEnabled
        device.batteryMonitoringEnabled = true

        fun read() {
            val level = device.batteryLevel
            val state = device.batteryState
            status = BatteryStatus(
                percentage = if (level >= 0f) (level * 100).roundToInt() else -1,
                isCharging = state == UIDeviceBatteryState.UIDeviceBatteryStateCharging,
                isFull = state == UIDeviceBatteryState.UIDeviceBatteryStateFull,
            )
        }
        read()

        val center = NSNotificationCenter.defaultCenter
        val observers = listOf(
            UIDeviceBatteryLevelDidChangeNotification,
            UIDeviceBatteryStateDidChangeNotification,
        ).map { name ->
            center.addObserverForName(name, null, NSOperationQueue.mainQueue) { read() }
        }

        onDispose {
            observers.forEach { center.removeObserver(it) }
            if (!wasEnabled) device.batteryMonitoringEnabled = false
        }
    }
    return status
}
