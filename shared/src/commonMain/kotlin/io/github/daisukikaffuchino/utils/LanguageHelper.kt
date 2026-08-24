package io.github.daisukikaffuchino.utils

import androidx.compose.ui.text.intl.Locale
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage

/** 平台当前生效的语言标签（BCP-47）。Android 走 AppCompat 的 per-app locale。 */
internal expect fun preferredLanguageTag(): String

/** 用户在设置里选的应用语言。 */
expect fun currentAppLanguage(): AppLanguage

/** 切换应用语言；返回 true 表示要重启才能完全生效。 */
expect suspend fun selectAppLanguage(language: AppLanguage): Boolean

/**
 * 启动时把用户存的语言重新应用一次。
 *
 * Android 有 AppCompatDelegate 自己记着，桌面要重新 Locale.setDefault，
 * iOS 靠启动前写好的 NSUserDefaults，这里是空的。
 */
expect fun applyStoredAppLanguage()

object LanguageHelper {
    val preferredLanguage: Locale get() = Locale(preferredLanguageTag())
}

val Locale.Companion.CHINESE: Locale get() = Locale("zh")
val Locale.Companion.SIMPLIFIED_CHINESE: Locale get() = Locale("zh-CN")
val Locale.Companion.TRADITIONAL_CHINESE: Locale get() = Locale("zh-TW")
val Locale.Companion.ENGLISH: Locale get() = Locale("en")
val Locale.Companion.JAPANESE: Locale get() = Locale("ja")
