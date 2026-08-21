package io.github.daisukikaffuchino.utils

import androidx.compose.ui.text.intl.Locale

/** 平台当前生效的语言标签（BCP-47）。Android 走 AppCompat 的 per-app locale。 */
internal expect fun preferredLanguageTag(): String

object LanguageHelper {
    val preferredLanguage: Locale get() = Locale(preferredLanguageTag())
}

val Locale.Companion.CHINESE: Locale get() = Locale("zh")
val Locale.Companion.SIMPLIFIED_CHINESE: Locale get() = Locale("zh-CN")
val Locale.Companion.TRADITIONAL_CHINESE: Locale get() = Locale("zh-TW")
val Locale.Companion.ENGLISH: Locale get() = Locale("en")
val Locale.Companion.JAPANESE: Locale get() = Locale("ja")
