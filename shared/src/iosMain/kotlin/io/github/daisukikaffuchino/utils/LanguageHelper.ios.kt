package io.github.daisukikaffuchino.utils

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

internal actual fun preferredLanguageTag(): String =
    NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"
