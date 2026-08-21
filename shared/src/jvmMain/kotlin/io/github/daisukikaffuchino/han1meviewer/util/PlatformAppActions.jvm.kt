package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable
import kotlin.system.exitProcess

// TODO(JVM): 桌面端重启需要外部拉起进程，这里先直接退出
actual fun restartApplication(): Unit = exitProcess(0)

@Composable
actual fun rememberExitApp(): () -> Unit = { exitProcess(0) }
