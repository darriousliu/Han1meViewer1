package io.github.daisukikaffuchino.han1meviewer.logic.network

import android.net.ConnectivityManager
import io.github.daisukikaffuchino.utils.applicationContext

actual fun isActiveNetworkMetered(): Boolean = runCatching {
    val cm = applicationContext.getSystemService(ConnectivityManager::class.java)
    cm?.isActiveNetworkMetered == true
}.getOrDefault(false)
