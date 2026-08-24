package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * 反复调没有副作用：系统只在第一次弹框，之后直接回上次的结果。
 *
 * 回调不在主线程上，而 [onDenied] 那头是要写 Compose 状态的，得切回主队列。
 */
@Composable
actual fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)? {
    val currentOnDenied by rememberUpdatedState(onDenied)
    return remember {
        {
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or
                        UNAuthorizationOptionSound or
                        UNAuthorizationOptionBadge
            ) { granted, _ ->
                if (!granted) dispatch_async(dispatch_get_main_queue()) { currentOnDenied() }
            }
        }
    }
}
