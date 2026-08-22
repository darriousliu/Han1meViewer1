package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import io.github.daisukikaffuchino.han1meviewer.ui.crash.installUncaughtExceptionHandler
import io.github.daisukikaffuchino.han1meviewer.ui.screen.crash.CrashScreenHost
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 崩溃是在出事的那个线程上报回来的，不能直接写 Compose 状态，
 * 先落到 StateFlow 上，由组合侧观察。
 */
private val crashFlow = MutableStateFlow<Throwable?>(null)

/**
 * 入口用 Nucleus 的 [nucleusApplication] 而不是 Compose 的 application：
 * composewebview 的桌面后端是 Nucleus Tao，它注册的 MainDispatcherFactory 优先级 100
 * （coroutines-swing 是 0），Dispatchers.Main 会被它接管。不走这个入口的话
 * Tao 没初始化，Main 指向一个不可用的调度器，而 Compose 的窗口建在 AWT EDT 上，
 * androidx.lifecycle 的主线程校验第一帧就抛 addObserver must be called on the main thread。
 *
 * NucleusApplicationScope 继承自 Compose 的 ApplicationScope，所以 Window / exitApplication 照旧。
 */
fun main(args: Array<String>) {
    // 越早装越好，UI 起来之前的崩溃也要接得住
    installUncaughtExceptionHandler { crashFlow.value = it }

    nucleusApplication(args, backend = NucleusBackend.Tao) {
        val crash by crashFlow.collectAsState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Han1meViewer",
        ) {
            val throwable = crash
            if (throwable == null) App() else CrashScreenHost(throwable, ::exitApplication)
        }
    }
}
