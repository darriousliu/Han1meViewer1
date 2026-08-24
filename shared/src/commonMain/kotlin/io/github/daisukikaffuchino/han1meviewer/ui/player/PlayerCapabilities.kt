package io.github.daisukikaffuchino.han1meviewer.ui.player

/** 平台内核支持的最大倍速。 */
expect val maxPlaybackSpeed: Float

/** 这个平台可供切换的播放内核；只有一个内核时为空表，调用方据此隐藏该设置项。 */
expect val availablePlayerKernels: List<PlayerKernel>

/**
 * 投屏可用性。null 表示该平台根本没有投屏能力（整组隐藏），
 * false 表示平台支持但当前不可用（Android 缺 Google Play 服务，置灰并说明原因）。
 */
expect fun googleCastAvailability(): Boolean?
