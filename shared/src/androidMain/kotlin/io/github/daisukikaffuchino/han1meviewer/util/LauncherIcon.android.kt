package io.github.daisukikaffuchino.han1meviewer.util

import io.github.daisukikaffuchino.han1meviewer.HanimeApplication
import io.github.daisukikaffuchino.utils.applicationContext

actual fun switchLauncherIcon(alias: String) {
    (applicationContext as? HanimeApplication)?.switchLauncher(alias)
}

actual val isLauncherIconSwitchSupported: Boolean = true
