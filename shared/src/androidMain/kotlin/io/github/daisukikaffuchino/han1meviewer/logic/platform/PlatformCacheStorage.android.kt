package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.folderSize

actual suspend fun cacheFolderSize(): Long = applicationContext.cacheDir.folderSize

actual fun cacheFolderSizeBlocking(): Long = applicationContext.cacheDir.folderSize

actual suspend fun clearCacheFolder(): Boolean =
    applicationContext.cacheDir?.deleteRecursively() == true
