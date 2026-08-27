package io.github.daisukikaffuchino.han1meviewer.logic.update

import dev.nucleusframework.updater.NucleusUpdater
import dev.nucleusframework.updater.UpdateResult
import dev.nucleusframework.updater.provider.GitHubProvider
import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "InAppUpdate"

/**
 * 更新源。产物与 `latest*.yml` 都由 CI 传到这个仓库的 Release 上
 * （见 desktopApp/build.gradle.kts 里的 `publish { github { } }`）。
 */
private const val GITHUB_OWNER = "darriousliu"
private const val GITHUB_REPO = "Han1meViewer1"

private val updater by lazy {
    NucleusUpdater {
        provider = GitHubProvider(GITHUB_OWNER, GITHUB_REPO)
        // 与 latest*.yml 里的 version 比对。BuildConfig.VERSION_NAME 与
        // Config.App.desktopPackageVersion 同源，所以两边对得上。
        currentVersion = BuildConfig.VERSION_NAME
        // 有 .blockmap 就只下增量块，一个 156MB 的包通常只需要几 MB
        differentialDownload = true
    }
}

/**
 * `isUpdateSupported()` 会把「当前这份发行形态装不了更新」的情况挡掉：
 * `gradlew run` 起的开发态、解压即用的 Zip、以及商店分发的形态（Pkg/AppX/Snap）。
 * 这些情况下退回打开下载页才是对的。
 */
actual val supportsInAppUpdate: Boolean
    get() = runCatching { updater.isUpdateSupported() }
        .onFailure { LogUtil.w(TAG, "判定是否支持应用内更新时出错", it) }
        .getOrDefault(false)

actual suspend fun runInAppUpdate(onStage: (InAppUpdateStage) -> Unit): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            // NucleusUpdater 自己再查一次 latest*.yml：AppUpdateChecker 那份 COS JSON 里
            // 没有文件清单、sha512 和 blockmap，下载校验需要的元数据只有这边有。
            when (val result = updater.checkForUpdates()) {
                is UpdateResult.Available -> {
                    var downloaded: java.io.File? = null
                    updater.downloadUpdate(result.info).collect { progress ->
                        onStage(
                            InAppUpdateStage.Downloading(
                                percent = (progress.percent / 100.0).toFloat().coerceIn(0f, 1f),
                                differential = progress.isDifferential,
                            )
                        )
                        // 最后一次发射带着落盘的文件
                        progress.file?.let { downloaded = it }
                    }
                    val file = downloaded
                        ?: error("下载流结束但没有拿到文件")
                    onStage(InAppUpdateStage.Installing)
                    // 交给系统安装器后本进程会被替换重启，下面这行之后的代码正常不会执行
                    updater.installAndRestart(file)
                }

                is UpdateResult.NotAvailable ->
                    // COS 那边说有新版、GitHub Release 还没传上来，属于发布过程中的正常窗口期
                    error("Release 上还没有可用于自动更新的产物")

                is UpdateResult.Error -> throw result.exception
            }
        }.onFailure { LogUtil.w(TAG, "应用内更新失败，退回打开下载页", it) }
    }
