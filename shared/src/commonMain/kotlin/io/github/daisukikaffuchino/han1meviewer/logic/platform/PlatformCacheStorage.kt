package io.github.daisukikaffuchino.han1meviewer.logic.platform

/** 应用缓存目录占用，单位字节。 */
expect suspend fun cacheFolderSize(): Long

/** 清缓存前先看有没有东西可清，这里在主线程上同步取一次。 */
expect fun cacheFolderSizeBlocking(): Long

expect suspend fun clearCacheFolder(): Boolean
