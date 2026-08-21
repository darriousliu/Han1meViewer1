package io.github.daisukikaffuchino.han1meviewer

import android.webkit.CookieManager
import io.github.daisukikaffuchino.han1meviewer.logic.SettingsRepository
import io.github.daisukikaffuchino.han1meviewer.logic.network.HCookieJar

suspend fun logout() {
    SettingsRepository.update {
        it.copy(isAlreadyLogin = false, loginCookie = EMPTY_STRING, savedUserId = EMPTY_STRING)
    }
    HCookieJar.cookieMap.clear()
    CookieManager.getInstance().removeAllCookies(null)
}
