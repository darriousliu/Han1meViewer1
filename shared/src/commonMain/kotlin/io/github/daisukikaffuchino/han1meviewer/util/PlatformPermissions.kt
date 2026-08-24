package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

/** 下载通知权限；不需要申请（或平台没有）时返回 null。 */
@Composable
expect fun rememberRequestNotificationPermission(onDenied: () -> Unit): (() -> Unit)?
