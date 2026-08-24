package io.github.daisukikaffuchino.utils

import androidx.appcompat.app.AppCompatDelegate
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import io.github.daisukikaffuchino.han1meviewer.util.AppLanguageManager
import java.util.Locale

internal actual fun preferredLanguageTag(): String =
    (AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()).toLanguageTag()

actual fun currentAppLanguage(): AppLanguage = AppLanguageManager.current(applicationContext)

// AppCompatDelegate 会自己重建 Activity，不用重启进程
actual suspend fun selectAppLanguage(language: AppLanguage): Boolean {
    AppLanguageManager.select(applicationContext, language)
    return false
}

actual fun applyStoredAppLanguage() = AppLanguageManager.applyStoredLanguage(applicationContext)
