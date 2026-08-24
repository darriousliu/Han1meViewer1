package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

/** 设备当前时间文本，跟随系统 12/24 小时制。 */
@Composable
expect fun rememberDeviceTimeText(): String
