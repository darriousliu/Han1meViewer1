package io.github.daisukikaffuchino.han1meviewer.logic.platform

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.download_completed_s
import han1meviewer.shared.generated.resources.download_task_completed
import han1meviewer.shared.generated.resources.download_task_failed
import han1meviewer.shared.generated.resources.download_task_failed_s_reason_s
import han1meviewer.shared.generated.resources.unknown_download_error
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSUUID
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

internal actual suspend fun notifyDownloadFinished(name: String) = postNotification(
    title = getString(Res.string.download_task_completed),
    body = getString(Res.string.download_completed_s, name),
)

internal actual suspend fun notifyDownloadFailed(name: String, reason: String?) = postNotification(
    title = getString(Res.string.download_task_failed),
    body = getString(
        Res.string.download_task_failed_s_reason_s,
        name,
        reason ?: getString(Res.string.unknown_download_error),
    ),
)

/**
 * 前台时系统默认把通知吞掉，要靠 delegate 明说要展示。
 *
 * 挂在第一次发通知时而不是应用启动时：这条路只有下载会用到，没下过东西就不该去动
 * UNUserNotificationCenter 的全局 delegate。
 */
private fun postNotification(title: String, body: String) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    if (center.delegate == null) center.setDelegate(foregroundPresenter)
    val content = UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(body)
        setSound(UNNotificationSound.defaultSound)
    }
    // trigger 给 null 就是立刻送达
    val request = UNNotificationRequest.requestWithIdentifier(
        identifier = NSUUID().UUIDString,
        content = content,
        trigger = null,
    )
    center.addNotificationRequest(request, withCompletionHandler = null)
}

/**
 * 只能是 class 不能是 object：继承 NSObject 的类型没法做成 Kotlin 单例，
 * 静态分配降不成 ObjC 的 alloc，编译到链接阶段才会炸。
 * center.delegate 是 weak 的，实例要自己强引用住。
 */
private val foregroundPresenter by lazy { ForegroundPresenter() }

private class ForegroundPresenter : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit,
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionSound
        )
    }
}
