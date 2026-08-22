package io.github.daisukikaffuchino.han1meviewer.ui.crash

import android.content.Context
import android.content.Intent
import io.github.daisukikaffuchino.han1meviewer.ui.activity.CrashActivity

const val EXTRA_LOGS = "logs"

/**
 * 崩了就拉起 CrashActivity，然后照常交回系统的默认处理器
 * （它会把崩溃报给 logcat / Play Console，并结束进程）。
 */
fun installAndroidCrashHandler(context: Context) = installUncaughtExceptionHandler { throwable ->
    context.startActivity(
        Intent(context, CrashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_LOGS, throwable.stackTraceToString())
        }
    )
}
