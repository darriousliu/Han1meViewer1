package io.github.daisukikaffuchino.han1meviewer.logic.platform

/**
 * 下载完成/失败通知。
 *
 * Android 不走这条路：它的通知是在 HanimeDownloadWorker 里连着前台服务一起发的，
 * 还带进度条，跟这里「结束时提醒一下」的语义不是一回事。
 */
internal expect suspend fun notifyDownloadFinished(name: String)

/** [reason] 为 null 时用「未知错误」兜底。 */
internal expect suspend fun notifyDownloadFailed(name: String, reason: String?)
