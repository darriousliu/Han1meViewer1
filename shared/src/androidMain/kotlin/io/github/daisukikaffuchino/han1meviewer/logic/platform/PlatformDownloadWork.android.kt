package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.worker.HanimeDownloadManager

actual val platformDownloadWorkController: DownloadWorkController
    get() = AndroidDownloadWorkController

actual fun setMaxConcurrentDownloadCount(value: Int) {
    HanimeDownloadManager.maxConcurrentDownloadCount = value
}

// 走自己读流的下载实现，能限速
actual val isDownloadSpeedLimitSupported: Boolean = true
