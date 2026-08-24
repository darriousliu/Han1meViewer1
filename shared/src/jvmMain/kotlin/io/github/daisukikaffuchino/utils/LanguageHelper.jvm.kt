package io.github.daisukikaffuchino.utils

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import java.util.Locale

internal actual fun preferredLanguageTag(): String = Locale.getDefault().toLanguageTag()

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
