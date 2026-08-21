package io.github.daisukikaffuchino.han1meviewer.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.util.Date

@Composable
actual fun rememberDeviceTimeText(): String {
    val context = LocalContext.current
    var text by remember(context) { mutableStateOf(DateFormat.getTimeFormat(context).format(Date())) }
    LaunchedEffect(context) {
        while (true) {
            text = DateFormat.getTimeFormat(context).format(Date())
            delay(60_000L)
        }
    }
    return text
}
