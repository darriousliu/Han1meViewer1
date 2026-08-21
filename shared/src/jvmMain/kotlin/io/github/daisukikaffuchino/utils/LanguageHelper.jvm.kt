package io.github.daisukikaffuchino.utils

import java.util.Locale

internal actual fun preferredLanguageTag(): String = Locale.getDefault().toLanguageTag()
