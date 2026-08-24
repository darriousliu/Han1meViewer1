package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_not_support
import io.github.daisukikaffuchino.utils.SonnerToast
import java.awt.Desktop
import java.io.File
import kotlin.system.exitProcess

// TODO(JVM): 桌面端重启需要外部拉起进程，这里先直接退出
actual fun restartApplication(): Unit = exitProcess(0)

actual val canRestartApplication: Boolean = true

@Composable
actual fun rememberExitApp(): () -> Unit = { exitProcess(0) }

// 桌面端没有防截屏，也没有「重建 Activity」的概念，返回 null 让调用方隐藏/跳过
@Composable
actual fun rememberSetSecureMode(): ((Boolean) -> Unit)? = null

@Composable
actual fun rememberRecreateScreen(): (() -> Unit)? = null

// 桌面端没有系统级默认打开方式设置，返回 null 让设置项直接不显示
@Composable
actual fun rememberOpenDeepLinkSettings(): (() -> Unit)? = null

/** 桌面端交给系统关联的播放器打开，没有 Android 那种选择器，chooserTitle 用不上。 */
actual fun openInExternalPlayer(
    videoUri: String,
    chooserTitle: String,
    onVideoMissing: () -> Unit,
) {
    val file = File(videoUri.removePrefix("file://"))
    if (!file.isFile) {
        onVideoMissing()
        return
    }
    val desktop = runCatching {
        Desktop.getDesktop().takeIf { it.isSupported(Desktop.Action.OPEN) }
    }.getOrNull()
    if (desktop == null) {
        SonnerToast.warning(Res.string.action_not_support)
        return
    }
    runCatching { desktop.open(file) }
        .onFailure { SonnerToast.warning(Res.string.action_not_support) }
}
