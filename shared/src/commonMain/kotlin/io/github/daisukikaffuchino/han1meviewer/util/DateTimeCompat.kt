package io.github.daisukikaffuchino.han1meviewer.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.TimeSource
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.number

/**
 * java.time 在 KMP 下不可用，这里补齐迁移中用到的那几个操作。
 */

private val systemZone: TimeZone get() = TimeZone.currentSystemDefault()

fun today(): LocalDate = Clock.System.todayIn(systemZone)

fun nowDateTime(): LocalDateTime = Clock.System.now().toLocalDateTime(systemZone)

fun nowTime(): LocalTime = nowDateTime().time

fun currentYearMonth(): YearMonth = today().let { YearMonth(it.year, it.month) }

fun YearMonth.atDay(day: Int): LocalDate = LocalDate(year, month, day)

fun YearMonth.plusMonths(months: Int): YearMonth =
    firstDay.plus(months, DateTimeUnit.MONTH).let { YearMonth(it.year, it.month) }

fun YearMonth.minusMonths(months: Int): YearMonth = plusMonths(-months)

/** a 到 b 相差几个月，等价于 ChronoUnit.MONTHS.between。 */
fun monthsBetween(from: YearMonth, to: YearMonth): Int = from.firstDay.monthsUntil(to.firstDay)

fun LocalDate.plusDays(days: Int): LocalDate = plus(days, DateTimeUnit.DAY)

/** 两位补零，替代 DateTimeFormatter 的 MM/dd。 */
fun Int.pad2(): String = if (this < 10) "0$this" else toString()

/** yyyy-MM */
fun YearMonth.toYearMonthString(): String = "$year-${month.number.pad2()}"

/** HH:mm */
fun LocalTime.toHourMinuteString(): String = "${hour.pad2()}:${minute.pad2()}"

/** 当天零点的 epoch 毫秒。 */
fun LocalDate.atStartOfDayEpochMillis(): Long =
    atTime(0, 0).toInstant(systemZone).toEpochMilliseconds()


/**
 * 按 CLDR 模式格式化日期，走平台的日期格式化器，星期/月份名跟随系统语言。
 * MM/dd/EEEE/yyyy 这些模式字在 java.time 和 NSDateFormatter 下语义一致。
 */
expect fun LocalDate.formatPattern(pattern: String): String

/** 单调递增的毫秒数，用于计时/节流；替代 Android 的 SystemClock.uptimeMillis()。 */
private val monotonicOrigin = TimeSource.Monotonic.markNow()

fun monotonicMillis(): Long = monotonicOrigin.elapsedNow().inWholeMilliseconds

/** epoch 毫秒 -> "yyyy-MM-dd HH:mm"，固定格式，不随语言变。 */
fun Long.toDateTimeText(): String {
    val dt = Instant.fromEpochMilliseconds(this).toLocalDateTime(systemZone)
    return "${dt.year}-${dt.month.number.pad2()}-${dt.day.pad2()} " +
            "${dt.hour.pad2()}:${dt.minute.pad2()}"
}

