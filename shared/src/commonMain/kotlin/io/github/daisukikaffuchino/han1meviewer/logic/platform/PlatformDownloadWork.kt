package io.github.daisukikaffuchino.han1meviewer.logic.platform

/** 平台的下载任务控制器。 */
expect val platformDownloadWorkController: DownloadWorkController

expect fun setMaxConcurrentDownloadCount(value: Int)

/**
 * 平台能不能限下载速度。
 *
 * iOS 走 NSURLSession 的后台会话，传输由系统负责、拿不到字节流，限速在那条路径上
 * 根本不存在，设置项就不该出现。
 */
expect val isDownloadSpeedLimitSupported: Boolean
