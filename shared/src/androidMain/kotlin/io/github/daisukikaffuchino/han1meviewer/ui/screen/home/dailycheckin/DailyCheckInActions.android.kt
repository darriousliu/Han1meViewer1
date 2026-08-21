package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.provider.CalendarContract
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.glance.appwidget.updateAll
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.no_calendar_app
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.ui.widget.CheckInWidget
import io.github.daisukikaffuchino.han1meviewer.util.atStartOfDayEpochMillis
import io.github.daisukikaffuchino.han1meviewer.util.plusDays
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.utils.application
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Composable
actual fun rememberAddCalendarEvent(): (LocalDate) -> Unit {
    val context = LocalContext.current
    return { date ->
        val intent = Intent(Intent.ACTION_INSERT).apply {
            setDataAndType(CalendarContract.Events.CONTENT_URI, "vnd.android.cursor.dir/event")
            putExtra(
                CalendarContract.Events.TITLE,
                context.getString(R.string.calendar_title, date.month.number, date.day)
            )
            putExtra(CalendarContract.Events.DESCRIPTION, context.getString(R.string.calendar_desc))
            putExtra(
                CalendarContract.Events.EVENT_LOCATION,
                context.getString(R.string.calendar_location)
            )
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.atStartOfDayEpochMillis())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.plusDays(1).atStartOfDayEpochMillis())
            putExtra(CalendarContract.Events.ALL_DAY, true)
            putExtra(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_FREE
            )
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            SonnerToast.warning(Res.string.no_calendar_app)
        }
    }
}

@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit {
    val activity = LocalActivity.current
    return { isFullscreen ->
        activity?.apply {
            requestedOrientation = if (isFullscreen) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            val bars = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.apply {
                    if (isFullscreen) {
                        hide(bars)
                        systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        show(bars)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                run {
                    window.decorView.systemUiVisibility = if (isFullscreen) {
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN
                    } else {
                        View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }
            }
        }
    }
}

actual suspend fun updateCheckInWidget() {
    runCatching { CheckInWidget().updateAll(application) }
}
