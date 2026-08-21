package io.github.daisukikaffuchino.han1meviewer.ui.screen.video

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberBatteryStatus(): BatteryStatus {
    val context = LocalContext.current
    var status by remember { mutableStateOf(BatteryStatus()) }

    DisposableEffect(context) {
        fun read(intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val state = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status = BatteryStatus(
                percentage = if (level >= 0 && scale > 0) level * 100 / scale else -1,
                isCharging = state == BatteryManager.BATTERY_STATUS_CHARGING,
                isFull = state == BatteryManager.BATTERY_STATUS_FULL,
            )
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = read(intent)
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        read(sticky)

        onDispose { context.unregisterReceiver(receiver) }
    }
    return status
}
