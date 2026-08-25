package io.github.daisukikaffuchino.utils

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import java.util.Locale

internal actual fun preferredLanguageTag(): String = Locale.getDefault().toLanguageTag()

actual fun currentAppLanguage(): AppLanguage = SettingsRepository.current.appLanguage

actual suspend fun selectAppLanguage(language: AppLanguage): Boolean {
    SettingsRepository.setLanguage(language)
    applyStoredAppLanguage()
    // 切到具体语言当场就生效；切回「跟随系统」不行：applyStoredAppLanguage 只会
    // setDefault 到某个具体 locale，系统那份在第一次切换时就被盖掉了，拿不回来
    return language == AppLanguage.SYSTEM
}

actual fun applyStoredAppLanguage() {
    val tag = SettingsRepository.current.appLanguage.code ?: return
    Locale.setDefault(Locale.forLanguageTag(tag))
}
