package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

// 没有需要动态申请的通知权限
@Composable
actual fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)? = null
