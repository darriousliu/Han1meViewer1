package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.daisukikaffuchino.han1meviewer.ui.crash.installUncaughtExceptionHandler
import io.github.daisukikaffuchino.han1meviewer.ui.screen.crash.CrashScreenHost
import kotlinx.coroutines.flow.MutableStateFlow
import io.github.daisukikaffuchino.han1meviewer.di.initAppOnce
import io.github.vinceglb.filekit.FileKit

/**
 * 崩溃是在出事的那个线程上报回来的，不能直接写 Compose 状态，
 * 先落到 StateFlow 上，由组合侧观察。
 */
private val crashFlow = MutableStateFlow<Throwable?>(null)

private const val APP_NAME = "Han1meViewer"

fun main() {
    // 越早装越好，UI 起来之前的崩溃也要接得住
    installUncaughtExceptionHandler { crashFlow.value = it }
    FileKit.init(APP_NAME)
    // 存储先于 Koin：Koin 的定义里有直接读 SettingsRepository 的
    initAppOnce()

    application {
        val crash by crashFlow.collectAsState()
        Window(
            onCloseRequest = ::exitApplication,
            title = APP_NAME,
        ) {
            val throwable = crash
            if (throwable == null) App() else CrashScreenHost(throwable, ::exitApplication)
        }
    }
}
