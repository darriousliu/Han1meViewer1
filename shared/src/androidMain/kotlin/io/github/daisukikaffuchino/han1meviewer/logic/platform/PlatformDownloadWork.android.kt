package io.github.daisukikaffuchino.han1meviewer.logic.platform

actual val platformDownloadWorkController: DownloadWorkController
    get() = AndroidDownloadWorkController
