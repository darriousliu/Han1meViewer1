package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.ui.window.ComposeUIViewController
import io.github.daisukikaffuchino.han1meviewer.di.initAppOnce
import platform.UIKit.UIViewController

/**
 * iosApp 的 `ContentView.swift` 通过 `MainViewControllerKt.MainViewController()` 调这里。
 * 改文件名会连带改 Swift 侧的类名（Kotlin 文件门面类 = 文件名 + Kt）。
 */
fun MainViewController(): UIViewController {
    // 必须在组合之外：放进 ComposeUIViewController 的 lambda 里就成了每次重组都跑
    initAppOnce()
    return ComposeUIViewController { App() }
}
