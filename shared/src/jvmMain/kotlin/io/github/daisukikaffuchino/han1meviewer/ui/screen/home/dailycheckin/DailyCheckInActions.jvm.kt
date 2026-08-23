package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.no_calendar_app
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberAddCalendarEvent(): (LocalDate) -> Unit {
    val scope = rememberCoroutineScope()
    return { date ->
        scope.launch {
            val invite = runCatching { buildCheckInInvite(date) }.getOrNull()
            val opened = invite != null && runCatching {
                Desktop.getDesktop().open(File(invite.path))
            }.isSuccess
            if (!opened) SonnerToast.warning(Res.string.no_calendar_app)
        }
    }
}

// TODO(JVM): 窗口全屏要拿到 desktopApp 的 WindowState，跟播放器全屏一起做
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = {}

// 桌面端没有小组件
actual suspend fun updateCheckInWidget() = Unit
