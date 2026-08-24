package io.github.daisukikaffuchino.han1meviewer.logic.platform

import androidx.glance.appwidget.updateAll
import io.github.daisukikaffuchino.han1meviewer.ui.widget.CheckInWidget
import io.github.daisukikaffuchino.utils.application

actual suspend fun updateCheckInWidget() {
    // 小组件刷新失败（宿主未就绪、没有已添加的小组件）不该把调用方带崩
    runCatching { CheckInWidget().updateAll(application) }
}
