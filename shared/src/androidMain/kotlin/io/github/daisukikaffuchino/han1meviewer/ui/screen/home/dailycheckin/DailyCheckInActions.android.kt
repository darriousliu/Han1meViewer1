package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.dailycheckin

import androidx.glance.appwidget.updateAll
import io.github.daisukikaffuchino.han1meviewer.ui.widget.CheckInWidget
import io.github.daisukikaffuchino.utils.application

actual suspend fun updateCheckInWidget() {
    runCatching { CheckInWidget().updateAll(application) }
}
