package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.daisukikaffuchino.han1meviewer.di.initAppOnce
import io.github.daisukikaffuchino.han1meviewer.ui.crash.installUncaughtExceptionHandler
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.installSystemDeepLinkHandlers
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.postDeepLinkFromArguments
import io.github.daisukikaffuchino.han1meviewer.ui.screen.crash.CrashScreenHost
import io.github.daisukikaffuchino.han1meviewer.ui.window.LocalDesktopWindow
import dev.nucleusframework.aot.runtime.AotRuntime
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.flow.MutableStateFlow
import java.awt.Dimension

/**
 * 崩溃是在出事的那个线程上报回来的，不能直接写 Compose 状态，
 * 先落到 StateFlow 上，由组合侧观察。
 */
private val crashFlow = MutableStateFlow<Throwable?>(null)

private const val APP_NAME = "Han1meViewer"

/**
 * 窗口最小尺寸。宽度压在 840dp（expanded 断点）之上，桌面就永远落在同一档
 * 宽度等级里：再窄下去先丢常驻侧栏、再丢播放页的推荐分栏，等于把桌面缩回手机布局。
 * 高度要放得下 Classic 横屏布局里 400dp 的播放器加下面的标签页。
 *
 * AWT 的 Dimension 走的是逻辑单位（Java 9 起自带 HiDPI 缩放），跟 Compose 的 dp 对得上。
 */
private const val MIN_WINDOW_WIDTH_DP = 900
private const val MIN_WINDOW_HEIGHT_DP = 640

/** 初始尺寸得大于上面的最小值，否则一显示就被顶到最小值；也别超出 1366x768 的小屏。 */
private val INITIAL_WINDOW_SIZE = DpSize(1024.dp, 720.dp)

fun main(args: Array<String>) {
    // Compose 1.11.1 的桌面无障碍层有空指针：a11y 焦点所在的节点被移除时，
    // defaultAccessibilityFocusTarget 会往 ArrayDeque 里塞 null 直接崩（进登录页必现）。
    // 1.11.1 已是 1.11 线最后一版，应用侧改不了，只能整层关掉。
    // 这条跟 libs.versions.toml 里 CMP 停在 1.11 是同一件事，升回 1.12 时一并复查。
    // 必须在 application {} 之前设：这个开关是懒读的，ComposeScene 一建就定死了。
    System.setProperty("compose.accessibility.enable", "false")
    // 越早装越好，UI 起来之前的崩溃也要接得住
    installUncaughtExceptionHandler { crashFlow.value = it }
    armAotTrainingExit()
    FileKit.init(APP_NAME)
    // 存储先于 Koin：Koin 的定义里有直接读 SettingsRepository 的
    initAppOnce()
    // DeepLinkBus 有 replay，先投也不会丢；macOS 的「打开方式」不进 argv，要挂 handler
    postDeepLinkFromArguments(args)
    installSystemDeepLinkHandlers()

    application {
        val crash by crashFlow.collectAsState()
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = INITIAL_WINDOW_SIZE),
            title = APP_NAME,
        ) {
            // 最小尺寸只有 AWT 那侧有，Compose 的 WindowState 管不到；
            // 放 LaunchedEffect 里是别在组合期直接改窗口。
            LaunchedEffect(window) {
                window.minimumSize = Dimension(MIN_WINDOW_WIDTH_DP, MIN_WINDOW_HEIGHT_DP)
            }
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

/**
 * AOT cache 的训练轮。
 *
 * 打包时 Nucleus 会把 `nucleus.aot.mode` 设成 `training` 再跑一次应用，记录类加载与 JIT
 * profile 落成 `app.aot`。**训练进程必须自己干净退出**，否则构建会一直等到 300 秒的兜底超时
 * 才把它杀掉，缓存也就白记了。
 *
 * 我们没有用 Nucleus 的 `aotTraining {}`——那个只在 `nucleusApplication {}` 里有，
 * 而桌面入口还是 Compose 原生的 `application {}`（换成 nucleusApplication 就等于换窗口后端，
 * 桌面播放页会黑屏，见 libs.versions.toml 里 nucleus 那段）。所以按官方文档给的
 * 「直接用 application {} 时自己驱动计时器」那条路走。
 *
 * 45 秒足够走完启动 → 首页首帧；非训练模式下这个函数什么都不做。
 */
private fun armAotTrainingExit() {
    if (!AotRuntime.isTraining()) return
    Thread({
        Thread.sleep(45_000)
        // 用 exitProcess 而不是 exitApplication：这里在组合之外，拿不到 ApplicationScope
        kotlin.system.exitProcess(0)
    }, "aot-training-exit").apply { isDaemon = true }.start()
}
