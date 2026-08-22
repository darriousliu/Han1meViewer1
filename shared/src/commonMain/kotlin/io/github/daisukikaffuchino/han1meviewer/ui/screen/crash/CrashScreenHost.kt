package io.github.daisukikaffuchino.han1meviewer.ui.screen.crash

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.daisukikaffuchino.han1meviewer.ui.crash.appPackageName
import io.github.daisukikaffuchino.han1meviewer.ui.crash.buildCrashReport
import io.github.daisukikaffuchino.han1meviewer.ui.theme.HanimeTheme
import io.github.daisukikaffuchino.han1meviewer.util.restartApplication
import io.github.daisukikaffuchino.utils.rememberCopyTextToClipboard
import kotlin.time.Clock

/**
 * 崩溃页的现成宿主：主题、报告拼装、复制都包好了，平台入口拿到异常直接塞进来即可。
 *
 * 用的是裸 MaterialTheme 而不是应用主题 —— 崩溃可能就出在配置读取那条链上，
 * 这里不能再依赖它。
 */
@Composable
fun CrashScreenHost(
    throwable: Throwable,
    onExitApp: () -> Unit,
    crashTimeMillis: Long = Clock.System.now().toEpochMilliseconds(),
) {
    HanimeTheme {
        val report = remember(throwable, crashTimeMillis) {
            buildCrashReport(
                crashLog = throwable.stackTraceToString(),
                crashTimeMillis = crashTimeMillis,
            )
        }
        val copyTextToClipboard = rememberCopyTextToClipboard()
        CrashScreen(
            crashReport = report,
            packageName = appPackageName,
            onCopyLog = { copyTextToClipboard(report) },
            onRestartApp = ::restartApplication,
            onExitApp = onExitApp,
        )
    }
}
