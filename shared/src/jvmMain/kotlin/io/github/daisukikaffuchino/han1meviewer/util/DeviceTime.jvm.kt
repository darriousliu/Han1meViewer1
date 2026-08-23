package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** 桌面端没有独立的 12/24 小时制开关，跟随系统区域设置。 */
private fun currentTimeText(): String = LocalTime.now().format(
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
)

@Composable
actual fun rememberDeviceTimeText(): String {
    var text by remember { mutableStateOf(currentTimeText()) }
    LaunchedEffect(Unit) {
        while (true) {
            // 每次重新取格式，用户中途改了区域也能跟上
            text = currentTimeText()
            delay(60_000L)
        }
    }
    return text
}
