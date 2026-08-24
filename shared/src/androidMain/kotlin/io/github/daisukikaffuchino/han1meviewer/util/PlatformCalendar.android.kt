package io.github.daisukikaffuchino.han1meviewer.util

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.calendar_desc
import han1meviewer.shared.generated.resources.calendar_location
import han1meviewer.shared.generated.resources.calendar_title
import han1meviewer.shared.generated.resources.no_calendar_app
import io.github.daisukikaffuchino.han1meviewer.util.atStartOfDayEpochMillis
import io.github.daisukikaffuchino.han1meviewer.util.plusDays
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.getString

@Composable
actual fun rememberAddCalendarEvent(): (LocalDate) -> Unit {
    val context = LocalContext.current
    return { date ->
        val intent = runBlocking {
            Intent(Intent.ACTION_INSERT).apply {
                setDataAndType(CalendarContract.Events.CONTENT_URI, "vnd.android.cursor.dir/event")
                putExtra(
                    CalendarContract.Events.TITLE,
                    getString(Res.string.calendar_title, date.month.number, date.day)
                )
                putExtra(CalendarContract.Events.DESCRIPTION, getString(Res.string.calendar_desc))
                putExtra(
                    CalendarContract.Events.EVENT_LOCATION,
                    getString(Res.string.calendar_location)
                )
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.atStartOfDayEpochMillis())
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.plusDays(1).atStartOfDayEpochMillis())
                putExtra(CalendarContract.Events.ALL_DAY, true)
                putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_FREE
                )
            }
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            SonnerToast.warning(Res.string.no_calendar_app)
        }
    }
}
