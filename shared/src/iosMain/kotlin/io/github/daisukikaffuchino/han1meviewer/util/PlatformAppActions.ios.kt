package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

// TODO(iOS): iOS 不允许应用自行重启/退出，这两个操作留空
actual fun restartApplication() = Unit

@Composable
actual fun rememberExitApp(): () -> Unit = {}

// iOS 没有防截屏，也没有「重建 Activity」的概念，返回 null 让调用方隐藏/跳过
@Composable
actual fun rememberSetSecureMode(): ((Boolean) -> Unit)? = null

@Composable
actual fun rememberRecreateScreen(): (() -> Unit)? = null

// iOS 没有对应的系统设置入口，返回 null 让设置项直接不显示
@Composable
actual fun rememberOpenDeepLinkSettings(): (() -> Unit)? = null
