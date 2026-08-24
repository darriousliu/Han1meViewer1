package io.github.daisukikaffuchino.han1meviewer.logic.platform

actual val platformDownloadWorkController: DownloadWorkController
    get() = FileDownloadWorkController

// 队列每次补人时现读 SettingsRepository.downloadCountLimit，
// 设置已经写进去了，这里不用再推一次
actual fun setMaxConcurrentDownloadCount(value: Int) = Unit

// 走自己读流的下载实现，能限速
actual val isDownloadSpeedLimitSupported: Boolean = true
