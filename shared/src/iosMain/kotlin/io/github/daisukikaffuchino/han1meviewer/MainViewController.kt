package io.github.daisukikaffuchino.han1meviewer

import androidx.compose.ui.window.ComposeUIViewController

/**
 * iosApp 的 `ContentView.swift` 通过 `MainViewControllerKt.MainViewController()` 调这里。
 * 改文件名会连带改 Swift 侧的类名（Kotlin 文件门面类 = 文件名 + Kt）。
 */
fun MainViewController() = ComposeUIViewController { App() }
