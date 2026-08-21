package io.github.daisukikaffuchino.han1meviewer.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter

actual fun LocalDate.formatPattern(pattern: String): String {
    val components = NSDateComponents().apply {
        year = this@formatPattern.year.toLong()
        month = this@formatPattern.month.number.toLong()
        day = this@formatPattern.day.toLong()
    }
    val date = NSCalendar.currentCalendar.dateFromComponents(components) ?: return toString()
    return NSDateFormatter().apply { dateFormat = pattern }.stringFromDate(date)
}
