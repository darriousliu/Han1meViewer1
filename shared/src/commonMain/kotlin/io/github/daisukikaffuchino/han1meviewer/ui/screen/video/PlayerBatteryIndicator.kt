package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.ic_battery_android_frame_1
import han1meviewer.shared.generated.resources.ic_battery_android_frame_2
import han1meviewer.shared.generated.resources.ic_battery_android_frame_3
import han1meviewer.shared.generated.resources.ic_battery_android_frame_4
import han1meviewer.shared.generated.resources.ic_battery_android_frame_5
import han1meviewer.shared.generated.resources.ic_battery_android_frame_6
import han1meviewer.shared.generated.resources.ic_battery_android_frame_bolt
import han1meviewer.shared.generated.resources.ic_battery_android_frame_full
import han1meviewer.shared.generated.resources.ic_battery_android_frame_question
import org.jetbrains.compose.resources.painterResource

/** 电量读数，percentage < 0 表示读不到。 */
data class BatteryStatus(
    val percentage: Int = -1,
    val isCharging: Boolean = false,
    val isFull: Boolean = false,
)

/** 读系统电量；平台读不到（桌面没有跨平台电量 API）时返回 null，整个指示器不显示。 */
@Composable
expect fun rememberBatteryStatus(): BatteryStatus?

@Composable
internal fun PlayerBatteryIndicator(modifier: Modifier = Modifier) {
    val status = if (LocalInspectionMode.current) {
        BatteryStatus(percentage = 100, isFull = true)
    } else {
        rememberBatteryStatus() ?: return
    }
    val iconResId = when {
        status.percentage < 0 -> Res.drawable.ic_battery_android_frame_question
        status.isCharging -> Res.drawable.ic_battery_android_frame_bolt
        status.isFull -> Res.drawable.ic_battery_android_frame_full
        status.percentage <= 15 -> Res.drawable.ic_battery_android_frame_1
        status.percentage <= 30 -> Res.drawable.ic_battery_android_frame_2
        status.percentage <= 45 -> Res.drawable.ic_battery_android_frame_3
        status.percentage <= 60 -> Res.drawable.ic_battery_android_frame_4
        status.percentage <= 75 -> Res.drawable.ic_battery_android_frame_5
        status.percentage <= 90 -> Res.drawable.ic_battery_android_frame_6
        else -> Res.drawable.ic_battery_android_frame_full
    }
    Icon(
        painter = painterResource(iconResId),
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.84f),
        modifier = modifier.size(20.dp),
    )
}
