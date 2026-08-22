package io.github.daisukikaffuchino.han1meviewer.ui.activity

import android.os.Bundle
import android.os.Process
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.stringResource
import io.github.daisukikaffuchino.han1meviewer.ui.crash.EXTRA_LOGS
import io.github.daisukikaffuchino.han1meviewer.ui.crash.buildCrashReport
import io.github.daisukikaffuchino.han1meviewer.ui.screen.crash.CrashScreen
import io.github.daisukikaffuchino.utils.ActivityManager
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.utils.rememberCopyTextToClipboard
import kotlin.system.exitProcess
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.crash_no_logs
import han1meviewer.shared.generated.resources.copy_to_clipboard

class CrashActivity : BaseActivity() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val crashLog = intent.getStringExtra(EXTRA_LOGS)
        val crashTimeMillis = System.currentTimeMillis()

        setHanimeContent {
            val noCrashLog = stringResource(Res.string.crash_no_logs)
            val report = remember(crashLog, crashTimeMillis, noCrashLog) {
                buildCrashReport(
                    crashLog = crashLog ?: noCrashLog,
                    crashTimeMillis = crashTimeMillis,
                )
            }
            val copyTextToClipboard = rememberCopyTextToClipboard()
            val exitApp = {
                finishAffinity()
                Process.killProcess(Process.myPid())
                exitProcess(0)
            }

            BackHandler(onBack = exitApp)
            CrashScreen(
                crashReport = report,
                packageName = packageName,
                onCopyLog = {
                    copyTextToClipboard(report)
                    SonnerToast.success(Res.string.copy_to_clipboard)
                },
                onRestartApp = { ActivityManager.restart(killProcess = true) },
                onExitApp = exitApp,
            )
        }
    }
}

