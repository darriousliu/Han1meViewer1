package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.size

actual suspend fun cacheFolderSize(): Long = FileKit.cacheDir.totalSize()

// 桌面/iOS 上目录遍历不涉及主线程阻塞的 binder 调用，直接算
actual fun cacheFolderSizeBlocking(): Long = FileKit.cacheDir.totalSize()

actual suspend fun clearCacheFolder(): Boolean = runCatching {
    FileKit.cacheDir.list().forEach { it.delete() }
}.isSuccess

private fun PlatformFile.totalSize(): Long = runCatching {
    list().sumOf { if (it.isDirectory()) it.totalSize() else it.size() }
}.getOrDefault(0L)
