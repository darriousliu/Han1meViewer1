package io.github.daisukikaffuchino.utils

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

internal actual fun preferredLanguageTag(): String =
    NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"

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
