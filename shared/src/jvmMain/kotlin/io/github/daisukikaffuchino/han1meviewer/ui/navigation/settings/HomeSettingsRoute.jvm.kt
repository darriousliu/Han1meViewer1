package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.size
import java.util.Locale

actual suspend fun cacheFolderSize(): Long = FileKit.cacheDir.totalSize()

// 桌面/iOS 上目录遍历不涉及主线程阻塞的 binder 调用，直接算
actual fun cacheFolderSizeBlocking(): Long = FileKit.cacheDir.totalSize()

actual suspend fun clearCacheFolder(): Boolean = runCatching {
    FileKit.cacheDir.list().forEach { it.delete() }
}.isSuccess

private fun PlatformFile.totalSize(): Long = runCatching {
    list().sumOf { if (it.isDirectory()) it.totalSize() else it.size() }
}.getOrDefault(0L)

actual fun currentAppLanguage(): AppLanguage = SettingsRepository.current.appLanguage

actual suspend fun selectAppLanguage(language: AppLanguage): Boolean {
    SettingsRepository.setLanguage(language)
    applyStoredAppLanguage()
    // Compose Resources 的语言环境在组合里被 remember 住了，改完要重启才全量生效
    return true
}

actual fun applyStoredAppLanguage() {
    val tag = SettingsRepository.current.appLanguage.code ?: return
    Locale.setDefault(Locale.forLanguageTag(tag))
}

actual suspend fun refreshCheckInWidget() {
}

actual fun switchLauncherIcon(alias: String) {
}

actual fun isDynamicColorSupported(): Boolean = false

// 桌面没有「换应用图标」这回事，设置项直接不渲染
actual val isLauncherIconSwitchSupported: Boolean = false
