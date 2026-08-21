package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

// TODO(ios): 跟随系统 12/24 小时制，暂时固定 24 小时制
@Composable
actual fun rememberDeviceTimeText(): String {
    var text by remember { mutableStateOf(nowTime().toHourMinuteString()) }
    LaunchedEffect(Unit) {
        while (true) {
            text = nowTime().toHourMinuteString()
            delay(60_000L)
        }
    }
    return text
}
