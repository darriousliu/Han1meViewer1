package io.github.daisukikaffuchino.han1meviewer

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.util.clearPlatformCookies

suspend fun logout() {
    SettingsRepository.update {
        it.copy(isAlreadyLogin = false, loginCookie = EMPTY_STRING, savedUserId = EMPTY_STRING)
    }
    clearPlatformCookies()
}
