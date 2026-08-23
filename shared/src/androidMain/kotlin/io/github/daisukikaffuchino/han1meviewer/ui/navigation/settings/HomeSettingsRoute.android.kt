package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import android.os.Build
import androidx.glance.appwidget.updateAll
import io.github.daisukikaffuchino.han1meviewer.HanimeApplication
import io.github.daisukikaffuchino.han1meviewer.logic.model.AppLanguage
import io.github.daisukikaffuchino.han1meviewer.ui.widget.CheckInWidget
import io.github.daisukikaffuchino.han1meviewer.util.AppLanguageManager
import io.github.daisukikaffuchino.utils.applicationContext
import io.github.daisukikaffuchino.utils.folderSize

actual suspend fun cacheFolderSize(): Long = applicationContext.cacheDir.folderSize

actual fun cacheFolderSizeBlocking(): Long = applicationContext.cacheDir.folderSize

actual suspend fun clearCacheFolder(): Boolean =
    applicationContext.cacheDir?.deleteRecursively() == true

actual fun currentAppLanguage(): AppLanguage = AppLanguageManager.current(applicationContext)

actual suspend fun selectAppLanguage(language: AppLanguage) {
    AppLanguageManager.select(applicationContext, language)
}

actual suspend fun refreshCheckInWidget() {
    CheckInWidget().updateAll(applicationContext)
}

actual fun switchLauncherIcon(alias: String) {
    (applicationContext as? HanimeApplication)?.switchLauncher(alias)
}

actual fun isDynamicColorSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
