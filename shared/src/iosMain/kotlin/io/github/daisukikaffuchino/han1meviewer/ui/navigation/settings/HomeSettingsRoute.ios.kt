package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import io.github.daisukikaffuchino.han1meviewer.logic.model.LauncherIconOption
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.size
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.alternateIconName
import platform.UIKit.setAlternateIconName
import platform.UIKit.supportsAlternateIcons

actual suspend fun cacheFolderSize(): Long = FileKit.cacheDir.totalSize()

// 桌面/iOS 上目录遍历不涉及主线程阻塞的 binder 调用，直接算
actual fun cacheFolderSizeBlocking(): Long = FileKit.cacheDir.totalSize()

actual suspend fun clearCacheFolder(): Boolean = runCatching {
    FileKit.cacheDir.list().forEach { it.delete() }
}.isSuccess

private fun PlatformFile.totalSize(): Long = runCatching {
    list().sumOf { if (it.isDirectory()) it.totalSize() else it.size() }
}.getOrDefault(0L)

private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

actual fun currentAppLanguage(): AppLanguage = SettingsRepository.current.appLanguage

actual suspend fun selectAppLanguage(language: AppLanguage): Boolean {
    SettingsRepository.setLanguage(language)
    applyStoredAppLanguage()
    return true
}

/**
 * iOS 没有运行时切换应用语言的 API，只能写 NSUserDefaults 的 AppleLanguages，
 * Foundation 在下次启动时按它决定 preferredLanguages。
 */
actual fun applyStoredAppLanguage() {
    val defaults = NSUserDefaults.standardUserDefaults
    val tag = SettingsRepository.current.appLanguage.code
    if (tag == null) {
        defaults.removeObjectForKey(APPLE_LANGUAGES_KEY)
    } else {
        defaults.setObject(listOf(tag), APPLE_LANGUAGES_KEY)
    }
}

actual suspend fun refreshCheckInWidget() {
}

/** 备用图标在 Assets.xcassets 里，由 ASSETCATALOG_COMPILER_ALTERNATE_APPICON_NAMES 打进包。 */
actual fun switchLauncherIcon(alias: String) {
    val application = UIApplication.sharedApplication
    if (!application.supportsAlternateIcons) return
    val target = LauncherIconOption.fromAlias(alias).iosIconName
    if (application.alternateIconName == target) return
    // 必须在主线程调；调用方是 rememberCoroutineScope()，默认就在主线程
    application.setAlternateIconName(target, null)
}

actual fun isDynamicColorSupported(): Boolean = false

actual val isLauncherIconSwitchSupported: Boolean = true
