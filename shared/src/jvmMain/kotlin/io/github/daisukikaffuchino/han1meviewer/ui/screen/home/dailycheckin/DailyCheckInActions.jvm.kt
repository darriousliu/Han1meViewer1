package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

// TODO(JVM): 桌面端加系统日历事件，暂无实现
@Composable
actual fun rememberAddCalendarEvent(): (LocalDate) -> Unit = {}

// TODO(JVM): 桌面窗口全屏切换，暂无实现
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = {}

// 桌面端没有小组件
actual suspend fun updateCheckInWidget() = Unit
