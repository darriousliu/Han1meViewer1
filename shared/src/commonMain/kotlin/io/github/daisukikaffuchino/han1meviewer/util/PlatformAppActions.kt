package io.github.daisukikaffuchino.han1meviewer.util

import androidx.compose.runtime.Composable

/** 重启应用，切换站点后需要。 */
expect fun restartApplication()

/** 退出应用（Android 是 finish 当前 Activity）。 */
@Composable
expect fun rememberExitApp(): () -> Unit

/** 防截屏开关（Android 是 FLAG_SECURE）；平台不支持时返回 null，调用方据此隐藏该选项。 */
@Composable
expect fun rememberSetSecureMode(): ((Boolean) -> Unit)?

/** 重建当前界面（导入备份后刷新设置）；平台没有这个概念时返回 null。 */
@Composable
expect fun rememberRecreateScreen(): (() -> Unit)?

/** 打开「默认打开方式」系统设置；平台不支持时返回 null，调用方据此隐藏该选项。 */
@Composable
expect fun rememberOpenDeepLinkSettings(): (() -> Unit)?
