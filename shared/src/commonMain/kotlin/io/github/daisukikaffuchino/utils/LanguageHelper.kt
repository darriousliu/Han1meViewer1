package io.github.daisukikaffuchino.utils

import androidx.compose.ui.text.intl.Locale

/** 平台当前生效的语言标签（BCP-47）。Android 走 AppCompat 的 per-app locale。 */
internal expect fun preferredLanguageTag(): String

object LanguageHelper {
    val preferredLanguage: Locale get() = Locale(preferredLanguageTag())
}
