package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.action_not_support
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.daisukikaffuchino.utils.SonnerToast
import java.awt.Desktop
import java.io.File
import kotlin.system.exitProcess

/**
 * 桌面端自己拉起一个新进程再退出。
 *
 * 命令行优先用 ProcessHandle 拿当前进程的原样参数——打包成 .app / .exe 之后
 * command() 就是那个启动器，这么拉起来最准。有些平台读不到自身参数，退回用
 * java.home + classpath + 主类重建（gradle run 走的就是这条）。
 */
actual fun restartApplication() {
    restartCommand()?.let { command ->
        runCatching {
            ProcessBuilder(command)
                .directory(File(System.getProperty("user.dir").orEmpty()))
                .inheritIO()
                .start()
        }.onFailure { LogUtil.e("Restart", "拉起新进程失败", it) }
    } ?: LogUtil.e("Restart", "拼不出重启命令，只能直接退出")
    exitProcess(0)
}

private fun restartCommand(): List<String>? {
    val info = ProcessHandle.current().info()
    val command = info.command().orElse(null)
    val arguments = info.arguments().orElse(null)
    if (command != null && arguments != null) return listOf(command) + arguments

    val javaHome = System.getProperty("java.home") ?: return null
    val classPath = System.getProperty("java.class.path") ?: return null
    // sun.java.command 是「主类 + 程序参数」，取第一段就是主类
    val mainClass = System.getProperty("sun.java.command")?.substringBefore(' ')
        ?.takeIf { it.isNotBlank() } ?: return null
    val javaBin = File(File(javaHome, "bin"), "java").path
    return listOf(javaBin, "-cp", classPath, mainClass)
}

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
