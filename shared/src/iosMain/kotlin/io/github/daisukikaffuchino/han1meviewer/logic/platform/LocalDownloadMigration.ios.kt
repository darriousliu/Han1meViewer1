package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.utils.LogUtil
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager

// moveItemAtPath 遇到目标已存在会直接失败，先清掉
@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun movePlatformFile(from: PlatformFile, to: PlatformFile): Boolean {
    val manager = NSFileManager.defaultManager
    if (manager.fileExistsAtPath(to.path)) {
        manager.removeItemAtPath(to.path, error = null)
    }
    val moved = manager.moveItemAtPath(from.path, toPath = to.path, error = null)
    if (!moved) LogUtil.e("Migrate", "搬运失败: ${from.path}")
    return moved
}
