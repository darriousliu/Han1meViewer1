package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.no_calendar_app
import io.github.daisukikaffuchino.han1meviewer.util.topMostViewController
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

/** 用分享面板把 .ics 递给系统日历，比 EventKit 省一次权限申请。 */
@Composable
actual fun rememberAddCalendarEvent(): (LocalDate) -> Unit {
    val scope = rememberCoroutineScope()
    return { date ->
        scope.launch {
            val invite = runCatching { buildCheckInInvite(date) }.getOrNull()
            val host = topMostViewController()
            if (invite == null || host == null) {
                SonnerToast.warning(Res.string.no_calendar_app)
            } else {
                val controller = UIActivityViewController(
                    activityItems = listOf(NSURL.fileURLWithPath(invite.path)),
                    applicationActivities = null,
                )
                // iPad 上 popover 没有锚点会直接崩
                controller.popoverPresentationController?.sourceView = host.view
                host.presentViewController(controller, animated = true, completion = null)
            }
        }
    }
}

// TODO(iOS): 锁横屏 + 隐藏状态栏要动 UIViewController，跟播放器全屏一起做
@Composable
actual fun rememberReportWindowMode(): (Boolean) -> Unit = {}

// 按约定不做 WidgetKit 小组件
actual suspend fun updateCheckInWidget() = Unit
