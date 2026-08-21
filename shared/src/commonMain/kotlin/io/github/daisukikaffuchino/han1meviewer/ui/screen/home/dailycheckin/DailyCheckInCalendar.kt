package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.YearMonth
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.fri
import han1meviewer.shared.generated.resources.mon
import han1meviewer.shared.generated.resources.sat
import han1meviewer.shared.generated.resources.sun
import han1meviewer.shared.generated.resources.thu
import han1meviewer.shared.generated.resources.tue
import han1meviewer.shared.generated.resources.wed
import io.github.daisukikaffuchino.han1meviewer.util.currentYearMonth
import io.github.daisukikaffuchino.han1meviewer.util.today
import io.github.daisukikaffuchino.han1meviewer.util.atDay
import io.github.daisukikaffuchino.han1meviewer.ui.component.rememberHapticPerformer

/**
 * 月历网格组件。展示指定月份的日期格，区分已打卡/今天/未来三种状态。
 *
 * @param yearMonth 目标月份
 * @param records 各日期打卡记录数
 * @param today 今天的日期
 * @param onDateClick 日期点击回调
 * @param onDateLongClick 日期长按回调
 * @param modifier 修饰符
 */
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    records: Map<LocalDate, Int>,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    onDateLongClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticPerformer()
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.numberOfDays
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        userScrollEnabled = false,
        modifier = modifier.fillMaxWidth()
    ) {
        item(span = { GridItemSpan(7) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf(
                    stringResource(Res.string.mon), stringResource(Res.string.tue),
                    stringResource(Res.string.wed), stringResource(Res.string.thu),
                    stringResource(Res.string.fri), stringResource(Res.string.sat),
                    stringResource(Res.string.sun)
                ).forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        items(firstDayOfWeek - 1) {
            Spacer(modifier = Modifier.size(48.dp))
        }

        items(daysInMonth) { day ->
            val date = yearMonth.atDay(day + 1)
            val count = records[date] ?: 0
            val isToday = date == today
            val transition = updateTransition(targetState = count > 0, label = "check")

            val bgColor by transition.animateColor(label = "bg") { checked ->
                when {
                    checked -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                    else -> Color.Transparent
                }
            }
            val borderColor by transition.animateColor(label = "border") { checked ->
                when {
                    isToday && checked -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.tertiary
                    checked -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                }
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(
                        width = if (isToday) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .combinedClickable(
                        onClick = {
                            haptic()
                            onDateClick(date)
                        },
                        onLongClick = {
                            haptic()
                            onDateLongClick(date)
                        },
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (day + 1).toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isToday -> MaterialTheme.colorScheme.onTertiaryContainer
                            count > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (count > 0) {
                        Text(
                            text = "x$count",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCalendarGrid() {
    CalendarGrid(
        yearMonth = currentYearMonth(),
        records = mapOf(today() to 3),
        today = today(),
        onDateClick = {},
        onDateLongClick = {},
    )
}
