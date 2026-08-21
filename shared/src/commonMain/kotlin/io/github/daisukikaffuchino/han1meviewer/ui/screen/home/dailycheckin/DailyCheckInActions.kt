package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

/** 往系统日历里加一条打卡提醒。 */
@Composable
expect fun rememberAddCalendarEvent(): (LocalDate) -> Unit

/** 年度报告切换全屏时调整屏幕方向与系统栏。 */
@Composable
expect fun rememberReportWindowMode(): (Boolean) -> Unit

/** 打卡数据变了以后刷新桌面小组件。 */
expect suspend fun updateCheckInWidget()
