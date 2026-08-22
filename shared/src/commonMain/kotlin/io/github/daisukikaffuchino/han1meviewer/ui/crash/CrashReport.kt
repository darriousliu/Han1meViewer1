package io.github.daisukikaffuchino.han1meviewer.ui.crash

import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.han1meviewer.util.pad2
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** 应用包名。BuildConfig 是 internal，模块外要用得走这里。 */
val appPackageName: String get() = BuildConfig.APPLICATION_ID

/** 报告头里的平台信息，每项一行，比如设备型号和系统版本。 */
expect fun crashReportPlatformInfo(): List<String>

/**
 * 崩溃报告正文。除了平台信息那几行，其余各端一致，
 * 这样不同平台捞回来的日志能对着看。
 */
fun buildCrashReport(crashLog: String, crashTimeMillis: Long): String = buildString {
    appendLine("App: Han1meViewer ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    appendLine("Package: ${BuildConfig.APPLICATION_ID}")
    crashReportPlatformInfo().forEach(::appendLine)
    appendLine("Crash time: ${crashTimeMillis.toCrashTimeText()}")
    appendLine()
    appendLine("====== beginning of crash ======")
    append(crashLog)
}

// 复用 toDateTimeText() 会丢秒，崩溃报告要精确到秒
private fun Long.toCrashTimeText(): String {
    val dt = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.year}-${dt.month.number.pad2()}-${dt.day.pad2()} " +
            "${dt.hour.pad2()}:${dt.minute.pad2()}:${dt.second.pad2()}"
}
