package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import kotlin.system.exitProcess

// TODO(JVM): 桌面端重启需要外部拉起进程，这里先直接退出
actual fun restartApplication(): Unit = exitProcess(0)

@Composable
actual fun rememberExitApp(): () -> Unit = { exitProcess(0) }

// TODO(jvm): 桌面端没有防截屏/重建界面
@Composable
actual fun rememberSetSecureMode(): (Boolean) -> Unit = {}

@Composable
actual fun rememberRecreateScreen(): () -> Unit = {}

// 桌面端没有系统级默认打开方式设置，返回 null 让设置项直接不显示
@Composable
actual fun rememberOpenDeepLinkSettings(): (() -> Unit)? = null
