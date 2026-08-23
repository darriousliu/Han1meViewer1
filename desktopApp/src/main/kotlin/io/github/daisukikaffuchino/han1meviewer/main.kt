package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.daisukikaffuchino.han1meviewer.di.initAppOnce
import io.github.daisukikaffuchino.han1meviewer.ui.crash.installUncaughtExceptionHandler
import io.github.daisukikaffuchino.han1meviewer.ui.screen.crash.CrashScreenHost
import io.github.daisukikaffuchino.han1meviewer.ui.window.LocalDesktopWindow
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 崩溃是在出事的那个线程上报回来的，不能直接写 Compose 状态，
 * 先落到 StateFlow 上，由组合侧观察。
 */
private val crashFlow = MutableStateFlow<Throwable?>(null)

private const val APP_NAME = "Han1meViewer"

fun main() {
    // Compose 1.11.1 的桌面无障碍层有空指针：a11y 焦点所在的节点被移除时，
    // defaultAccessibilityFocusTarget 会往 ArrayDeque 里塞 null 直接崩（进登录页必现）。
    // 1.11.1 已是 1.11 线最后一版，应用侧改不了，只能整层关掉。
    // 这条跟 libs.versions.toml 里 CMP 停在 1.11 是同一件事，升回 1.12 时一并复查。
    // 必须在 application {} 之前设：这个开关是懒读的，ComposeScene 一建就定死了。
    System.setProperty("compose.accessibility.enable", "false")
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
            // 播放页全屏要拿到窗口，Compose Desktop 只能从这里往下传
            CompositionLocalProvider(LocalDesktopWindow provides window) {
                // 崩溃页刻意不进 HanimeAppRoot：崩的可能就是主题/配置那条链
                if (throwable == null) App()
                else CrashScreenHost(throwable, ::exitApplication)
            }
        }
    }
}
