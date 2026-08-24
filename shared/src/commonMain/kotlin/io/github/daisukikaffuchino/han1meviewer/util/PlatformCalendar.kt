package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

/** 往系统日历里加一条打卡提醒。 */
@Composable
expect fun rememberAddCalendarEvent(): (LocalDate) -> Unit
