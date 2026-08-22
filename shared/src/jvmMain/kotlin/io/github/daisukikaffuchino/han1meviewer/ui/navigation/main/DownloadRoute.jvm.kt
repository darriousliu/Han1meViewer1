package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import io.github.daisukikaffuchino.han1meviewer.logic.dao.download.HanimeDownloadDao

// TODO(jvm): 外部播放器与下载目录扫描还没实现
actual fun openInExternalPlayer(
    videoUri: String,
    chooserTitle: String,
    onVideoMissing: () -> Unit,
) {
    onVideoMissing()
}

actual suspend fun deleteDownloadVideoFolder(videoCode: String) {
}

actual suspend fun importDownloadedVideos(dao: HanimeDownloadDao): Boolean = false
