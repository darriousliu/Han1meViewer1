package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// TODO(ios): 后台下载任务尚未实现
private object NoopDownloadWorkController : DownloadWorkController {
    override fun prune() = Unit
    override suspend fun initialize() = Unit
    override fun runningCount(): Flow<Int> = flowOf(0)
    override fun pause(entity: HanimeDownloadEntity) = Unit
    override fun resume(entity: HanimeDownloadEntity) = Unit
    override fun delete(entity: HanimeDownloadEntity) = Unit
}

actual val platformDownloadWorkController: DownloadWorkController
    get() = NoopDownloadWorkController
