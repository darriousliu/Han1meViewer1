package io.github.daisukikaffuchino.han1meviewer.util

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.calendar_desc
import han1meviewer.shared.generated.resources.calendar_location
import han1meviewer.shared.generated.resources.calendar_title
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.write
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.getString

/**
 * 桌面端用：没有 Android 那种「打开日历应用插入界面」的 Intent，但系统认 .ics，
 * 写一个临时文件交给关联程序打开即可。
 *
 * iOS 不走这条路——那边用 EventKit 真正写进日历，见 [rememberAddCalendarEvent]。
 */
internal suspend fun buildCheckInInvite(date: LocalDate): PlatformFile {
    val title = getString(Res.string.calendar_title, date.month.number, date.day)
    val description = getString(Res.string.calendar_desc)
    val location = getString(Res.string.calendar_location)

    // 全天事件用 VALUE=DATE，DTEND 是开区间所以要 +1 天
    val ics = buildString {
        appendLine("BEGIN:VCALENDAR")
        appendLine("VERSION:2.0")
        appendLine("PRODID:-//Han1meViewer//Check-in//EN")
        appendLine("BEGIN:VEVENT")
        appendLine("UID:checkin-${date}@han1meviewer")
        appendLine("DTSTART;VALUE=DATE:${date.compact()}")
        appendLine("DTEND;VALUE=DATE:${date.plusDays(1).compact()}")
        appendLine("SUMMARY:${title.escapeIcs()}")
        appendLine("DESCRIPTION:${description.escapeIcs()}")
        appendLine("LOCATION:${location.escapeIcs()}")
        appendLine("TRANSP:TRANSPARENT")
        appendLine("END:VEVENT")
        append("END:VCALENDAR")
    }
    // 用完即弃，放 cacheDir 正合适
    val file = FileKit.cacheDir / "checkin-$date.ics"
    file.write(ics.encodeToByteArray())
    return file
}

private fun LocalDate.compact(): String =
    "$year${month.number.toString().padStart(2, '0')}${day.toString().padStart(2, '0')}"

/** RFC 5545 里 `\ ; , 换行` 要转义。 */
private fun String.escapeIcs(): String = this
    .replace("\\", "\\\\")
    .replace(";", "\\;")
    .replace(",", "\\,")
    .replace("\n", "\\n")
