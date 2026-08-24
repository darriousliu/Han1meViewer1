package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import androidx.glance.appwidget.updateAll
import io.github.daisukikaffuchino.han1meviewer.ui.widget.CheckInWidget
import io.github.daisukikaffuchino.utils.applicationContext

actual suspend fun refreshCheckInWidget() {
    CheckInWidget().updateAll(applicationContext)
}
