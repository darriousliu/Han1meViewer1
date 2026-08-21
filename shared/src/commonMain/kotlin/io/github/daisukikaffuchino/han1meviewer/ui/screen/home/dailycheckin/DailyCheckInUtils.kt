package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import io.github.daisukikaffuchino.han1meviewer.logic.entity.CheckInType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import net.sergeych.sprintf.sprintf
import io.github.daisukikaffuchino.han1meviewer.util.plusDays

/**
 * 热力图颜色梯度（0 → 4+ 次）。
 */
@Composable
internal fun rememberContributionColors(): List<Color> {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    return remember(primary, primaryContainer) {
        listOf(
            lerp(primaryContainer, primary, 0.15f),
            lerp(primaryContainer, primary, 0.35f),
            lerp(primaryContainer, primary, 0.55f),
            lerp(primaryContainer, primary, 0.75f),
            primary
        )
    }
}

/**
 * 根据打卡次数返回热力图颜色等级。
 *
 * @param count 打卡次数
 * @return 颜色等级 0–4
 */
internal fun getContributionLevel(count: Int): Int = when {
    count <= 0 -> 0
    count == 1 -> 1
    count == 2 -> 2
    count in 3..4 -> 3
    else -> 4
}

/**
 * 将打卡类型转换为对应 emoji。
 *
 * @param type [CheckInType.storeName] 值
 * @return 对应的 emoji 字符
 */
fun typeEmoji(type: String): String = when (type) {
    CheckInType.MASTURBATION.storeName -> "\uD83E\uDD1C"
    CheckInType.WET_DREAM.storeName -> "\uD83D\uDCA4"
    CheckInType.SEX.storeName -> "\uD83D\uDC91"
    CheckInType.ORAL.storeName -> "\uD83D\uDC45"
    CheckInType.OTHER.storeName -> "\u2753"
    else -> "\uD83D\uDCCA"
}

/**
 * 将一年按周分组，用于热力图渲染。
 *
 * @param year 目标年份
 * @return 每周 7 天的日期列表（null 表示该天不属于这一年）
 */
internal fun buildYearWeeks(year: Int): List<List<LocalDate?>> {
    val start = LocalDate(year, 1, 1)
    val end = LocalDate(year, 12, 31)
    val weeks = mutableListOf<MutableList<LocalDate?>>()
    var currentWeek = MutableList<LocalDate?>(7) { null }
    var dayIndex = start.dayOfWeek.isoDayNumber - 1
    var date = start
    while (date <= end) {
        currentWeek[dayIndex] = date
        dayIndex++
        if (dayIndex == 7) {
            weeks.add(currentWeek)
            currentWeek = MutableList(7) { null }
            dayIndex = 0
        }
        date = date.plusDays(1)
    }
    if (currentWeek.any { it != null }) {
        weeks.add(currentWeek)
    }
    return weeks
}

/**
 * 从周列表构建月份标签位置。
 *
 * @param year 目标年份
 * @param weeks 周列表（由 [buildYearWeeks] 生成）
 * @param monthFormat 月份格式化字符串
 * @return 月份标签与起始周索引的列表
 */
internal fun buildMonthLabels(
    year: Int,
    weeks: List<List<LocalDate?>>,
    monthFormat: String,
): List<Pair<String, Int>> {
    val labels = mutableListOf<Pair<String, Int>>()
    for (month in 1..12) {
        val firstDay = LocalDate(year, month, 1)
        val weekIdx = weeks.indexOfFirst { week -> firstDay in week }
        if (weekIdx >= 0) {
            labels.add(monthFormat.sprintf(month) to weekIdx)
        }
    }
    return labels
}
