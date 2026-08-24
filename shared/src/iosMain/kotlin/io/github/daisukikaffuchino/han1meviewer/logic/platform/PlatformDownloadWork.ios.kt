package io.github.daisukikaffuchino.han1meviewer.logic.platform

actual val platformDownloadWorkController: DownloadWorkController
    get() = NsUrlSessionDownloadController

// 队列每次补人时现读 SettingsRepository.downloadCountLimit，
// 设置已经写进去了，这里不用再推一次
actual fun setMaxConcurrentDownloadCount(value: Int) = Unit

// NSURLSession 后台会话由系统传输，拿不到字节流，限不了速
actual val isDownloadSpeedLimitSupported: Boolean = false
