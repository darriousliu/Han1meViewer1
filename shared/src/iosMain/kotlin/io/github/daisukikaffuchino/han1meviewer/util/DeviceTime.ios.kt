package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

/** shortStyle 会自动跟随系统设置里的 12/24 小时制。 */
private fun currentTimeText(): String = NSDateFormatter().apply {
    dateStyle = NSDateFormatterNoStyle
    timeStyle = NSDateFormatterShortStyle
    locale = NSLocale.currentLocale
}.stringFromDate(NSDate())

@Composable
actual fun rememberDeviceTimeText(): String {
    var text by remember { mutableStateOf(currentTimeText()) }
    LaunchedEffect(Unit) {
        while (true) {
            // 每次重建 formatter，用户中途改了 12/24 小时制也能跟上
            text = currentTimeText()
            delay(60_000L)
        }
    }
    return text
}
