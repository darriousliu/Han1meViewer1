package io.github.daisukikaffuchino.han1meviewer.logic.network

/**
 * 装上平台的网络配置（系统代理等），启动时调一次。
 *
 * 必须排在 SettingsRepository 安装之后 —— 代理选择器要读用户配置。
 */
expect fun installPlatformNetworking()

/** 用户改了代理/DNS 配置后重新构建平台网络栈。 */
expect fun rebuildPlatformNetworking()
