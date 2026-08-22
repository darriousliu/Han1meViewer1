package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import kotlinx.coroutines.flow.Flow

interface DownloadWorkController {
    fun prune()
    suspend fun initialize()
    fun runningCount(): Flow<Int>

    fun pause(entity: HanimeDownloadEntity)
    fun resume(entity: HanimeDownloadEntity)
    fun delete(entity: HanimeDownloadEntity)
}
