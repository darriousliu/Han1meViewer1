package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.size

// TODO(jvm): 备份导入导出还没实现
actual suspend fun exportBackupTo(file: PlatformFile) {
}

actual suspend fun importBackupFrom(file: PlatformFile) {
}

actual suspend fun cacheFolderSize(): Long = FileKit.cacheDir.totalSize()

// 桌面/iOS 上目录遍历不涉及主线程阻塞的 binder 调用，直接算
actual fun cacheFolderSizeBlocking(): Long = FileKit.cacheDir.totalSize()

actual suspend fun clearCacheFolder(): Boolean = runCatching {
    FileKit.cacheDir.list().forEach { it.delete() }
}.isSuccess

private fun PlatformFile.totalSize(): Long = runCatching {
    list().sumOf { if (it.isDirectory()) it.totalSize() else it.size() }
}.getOrDefault(0L)

// TODO(jvm): 应用内切换语言
actual fun currentAppLanguage(): AppLanguage = AppLanguage.SYSTEM

actual suspend fun selectAppLanguage(language: AppLanguage) {
}

actual suspend fun refreshCheckInWidget() {
}

actual fun switchLauncherIcon(alias: String) {
}

actual fun isDynamicColorSupported(): Boolean = false
