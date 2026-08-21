package io.github.daisukikaffuchino.utils

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

internal actual fun preferredLanguageTag(): String =
    (AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()).toLanguageTag()
