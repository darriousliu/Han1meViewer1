package io.github.daisukikaffuchino.han1meviewer.logic.platform

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.download_completed_s
import han1meviewer.shared.generated.resources.download_task_completed
import han1meviewer.shared.generated.resources.download_task_failed
import han1meviewer.shared.generated.resources.download_task_failed_s_reason_s
import han1meviewer.shared.generated.resources.unknown_download_error
import io.github.daisukikaffuchino.utils.LogUtil
import org.jetbrains.compose.resources.getString
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.RenderingHints
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage

private const val TRAY_TOOLTIP = "Han1meViewer"

internal actual suspend fun notifyDownloadFinished(name: String) = notify(
    title = getString(Res.string.download_task_completed),
    body = getString(Res.string.download_completed_s, name),
    type = TrayIcon.MessageType.INFO,
)

internal actual suspend fun notifyDownloadFailed(name: String, reason: String?) = notify(
    title = getString(Res.string.download_task_failed),
    body = getString(
        Res.string.download_task_failed_s_reason_s,
        name,
        reason ?: getString(Res.string.unknown_download_error),
    ),
    type = TrayIcon.MessageType.ERROR,
)

/**
 * 桌面没有系统级通知 API，只能挂个托盘图标再用它发消息——Compose Desktop 的
 * `TrayState.sendNotification` 底下也是这条 AWT 通道。
 *
 * 图标是画出来的：应用图标只有 webp 和矢量两种，ImageIO 都读不了，而托盘必须给一张
 * Image。托盘不可用（无头环境、Linux 上没有托盘）时整条静默跳过，只记一行日志。
 */
private val trayIcon: TrayIcon? by lazy {
    if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) return@lazy null
    runCatching {
        TrayIcon(trayImage(), TRAY_TOOLTIP).apply {
            isImageAutoSize = true
            SystemTray.getSystemTray().add(this)
        }
    }.onFailure {
        LogUtil.w("Download", "托盘不可用，下载通知发不出去", it)
    }.getOrNull()
}

private fun notify(title: String, body: String, type: TrayIcon.MessageType) {
    trayIcon?.displayMessage(title, body, type)
}

/** 圆角底 + 一个向下的箭头，够在托盘里认出来就行。 */
private fun trayImage(): Image {
    val size = 16
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.color = Color(0xE9, 0x1E, 0x63)
    g.fillRoundRect(0, 0, size, size, 6, 6)
    g.color = Color.WHITE
    g.fillRect(7, 3, 2, 6)
    g.fillPolygon(intArrayOf(4, 12, 8), intArrayOf(8, 8, 13), 3)
    g.dispose()
    return image
}
