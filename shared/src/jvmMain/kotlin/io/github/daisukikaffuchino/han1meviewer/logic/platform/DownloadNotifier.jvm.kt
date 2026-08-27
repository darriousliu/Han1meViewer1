package io.github.daisukikaffuchino.han1meviewer.logic.platform

import dev.nucleusframework.notification.common.NotificationManager
import dev.nucleusframework.notification.common.NotificationResult
import dev.nucleusframework.notification.common.notification
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.download_completed_s
import han1meviewer.shared.generated.resources.download_task_completed
import han1meviewer.shared.generated.resources.download_task_failed
import han1meviewer.shared.generated.resources.download_task_failed_s_reason_s
import han1meviewer.shared.generated.resources.unknown_download_error
import io.github.daisukikaffuchino.utils.LogUtil
import org.jetbrains.compose.resources.getString

private const val TAG = "Download"

/**
 * 桌面端的下载通知，走各平台的系统通知中心
 * （Windows Toast / macOS UNUserNotification / Linux libnotify）。
 *
 * 以前这里是自己挂一个 AWT 托盘图标、再用 `TrayIcon.displayMessage` 发消息——那是 Compose Desktop
 * 的 `TrayState.sendNotification` 底下同一条 AWT 通道，图标还得用 Graphics2D 现画（应用图标只有
 * webp 和矢量两种，ImageIO 都读不了）。换成 Nucleus 之后不再需要托盘常驻，图标由系统取应用自己的。
 *
 * ⚠️ macOS 上未打包的进程发通知会被系统静默丢弃，`./gradlew run` 里试不出效果，
 * 要用 `./gradlew runDistributable` 或直接跑安装后的 .app。
 */
internal actual suspend fun notifyDownloadFinished(name: String) = notify(
    title = getString(Res.string.download_task_completed),
    body = getString(Res.string.download_completed_s, name),
)

internal actual suspend fun notifyDownloadFailed(name: String, reason: String?) = notify(
    title = getString(Res.string.download_task_failed),
    body = getString(
        Res.string.download_task_failed_s_reason_s,
        name,
        reason ?: getString(Res.string.unknown_download_error),
    ),
)

/**
 * 首次发通知前初始化一次。
 *
 * `initialize()` 幂等，但不是线程安全的读改写，所以用 lazy 兜一层；顺带把「系统不支持通知」
 * （无头环境、Linux 上没有通知守护进程）这种情况一次性判掉，之后每条都直接跳过。
 */
private val available: Boolean by lazy {
    runCatching {
        NotificationManager.initialize()
        NotificationManager.isAvailable()
    }.onFailure {
        LogUtil.w(TAG, "系统通知不可用，下载通知发不出去", it)
    }.getOrDefault(false)
}

private fun notify(title: String, body: String) {
    if (!available) return
    val result = runCatching {
        notification(title = title, message = body).send()
    }.onFailure { LogUtil.w(TAG, "发通知失败", it) }.getOrNull()

    if (result is NotificationResult.Failure) {
        LogUtil.w(TAG, "系统拒绝了这条通知: $result")
    }
}
