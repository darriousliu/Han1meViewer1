package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

// TODO(iOS): iOS 不允许应用自行重启/退出，这两个操作留空
actual fun restartApplication() = Unit

@Composable
actual fun rememberExitApp(): () -> Unit = {}

// TODO(ios): iOS 没有防截屏/重建界面
@Composable
actual fun rememberSetSecureMode(): (Boolean) -> Unit = {}

@Composable
actual fun rememberRecreateScreen(): () -> Unit = {}

// iOS 没有对应的系统设置入口，返回 null 让设置项直接不显示
@Composable
actual fun rememberOpenDeepLinkSettings(): (() -> Unit)? = null
