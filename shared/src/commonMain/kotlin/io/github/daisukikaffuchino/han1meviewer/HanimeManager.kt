package io.github.daisukikaffuchino.han1meviewer

import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository

/** 清掉平台网络栈/WebView 里残留的 cookie。 */
internal expect suspend fun clearPlatformCookies()

suspend fun logout() {
    SettingsRepository.update {
        it.copy(isAlreadyLogin = false, loginCookie = EMPTY_STRING, savedUserId = EMPTY_STRING)
    }
    clearPlatformCookies()
}
