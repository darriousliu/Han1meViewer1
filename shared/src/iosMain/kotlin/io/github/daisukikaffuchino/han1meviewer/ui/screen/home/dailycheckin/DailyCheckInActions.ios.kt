package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

// TODO(iOS): 用 EventKit 加日历事件，需要先申请日历权限
@Composable
actual fun rememberAddCalendarEvent(): (LocalDate) -> Unit = {}

// TODO(iOS): 全屏时锁横屏 + 隐藏状态栏，暂无实现
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = {}

// TODO(iOS): WidgetKit 小组件刷新，暂无实现
actual suspend fun updateCheckInWidget() = Unit
