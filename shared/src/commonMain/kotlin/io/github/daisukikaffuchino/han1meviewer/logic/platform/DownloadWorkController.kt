package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.entity.download.HanimeDownloadEntity
import kotlinx.coroutines.flow.Flow

/** 新建下载任务的参数，各平台自行映射到自己的任务队列。 */
data class DownloadTaskArgs(
    val quality: String?,
    val downloadUrl: String?,
    val videoType: String?,
    val hanimeName: String,
    val videoCode: String,
    val coverUrl: String,
    val groupId: Int,
)

interface DownloadWorkController {
    fun prune()
    suspend fun initialize()
    fun runningCount(): Flow<Int>

    fun pause(entity: HanimeDownloadEntity)
    fun resume(entity: HanimeDownloadEntity)
    fun delete(entity: HanimeDownloadEntity)

    fun enqueue(args: DownloadTaskArgs, redownload: Boolean = false)
}
