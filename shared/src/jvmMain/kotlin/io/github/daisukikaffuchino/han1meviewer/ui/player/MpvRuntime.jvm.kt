package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import org.openani.mediamp.mpv.MPVHandle
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

private const val TAG = "MpvRuntime"

/** 解压完成的标记，缺了就说明上次解到一半，目录里的库不能信。 */
private const val MARKER_NAME = ".extracted"

private const val RUNTIME_DIR_NAME = "mpv-runtime"

/** 预热卡死时别把播放页一起拖住，超时就照常往下走（大不了退回 mediamp 自己那套）。 */
private const val PREWARM_TIMEOUT_SECONDS = 30L

private val prewarmStarted = AtomicBoolean(false)
private val prewarmFinished = CountDownLatch(1)

/**
 * 提前把 libmpv 的原生运行时解压并 dlopen 好，在后台线程上做。
 *
 * mediamp 默认是「第一次建播放器时才加载」，而且解压目标是
 * `Files.createTempDirectory("mediamp-mpv")`——**每次启动都是一个新目录**，于是每次启动
 * 首开播放页都要把 20 多 MB 的 libmpv/ffmpeg 重新铺一遍再 dlopen，macOS 上还要为这份
 * 「新文件」重跑一次 Gatekeeper 检查。这一整段是在播放页的 `remember` 里、也就是组合期
 * 发生的，所以直接表现为「进播放页卡 5–10 秒」。
 *
 * 这里改成固定目录（按应用版本分目录，升级自动换一份）：第一次启动仍要解压一次，
 * 之后就只剩 dlopen；而且都挪到了启动时的后台线程上，播放页不再等它。
 *
 * 只在桌面端调，重复调用是空操作。
 */
fun prewarmMpvRuntime() {
    if (!prewarmStarted.compareAndSet(false, true)) return
    thread(name = "mpv-runtime-prewarm", isDaemon = true) {
        val startedAt = System.nanoTime()
        try {
            loadRuntime(runtimeDir())
            val costMs = (System.nanoTime() - startedAt) / 1_000_000
            LogUtil.d(TAG, "libmpv 预热完成，耗时 ${costMs}ms")
        } catch (e: Throwable) {
            // 预热失败不影响功能：mediamp 建播放器时会自己再走一遍默认的临时目录那条路
            LogUtil.e(TAG, "libmpv 预热失败，播放页会退回懒加载", e)
        } finally {
            prewarmFinished.countDown()
        }
    }
}

/**
 * 等预热跑完再建播放器。
 *
 * 必须等：mediamp 的 `NativeRuntimeLoader` 只允许配置一个运行时目录，预热还没写下
 * 「已配置」标志时建播放器，它会拿默认临时目录再配一次，然后被
 * 「already loaded from ... cannot be reconfigured」挡下来，播放页当场变成错误态。
 *
 * 正常情况下预热在启动时就跑完了，这里直接返回。
 */
internal fun awaitMpvRuntimePrewarm() {
    if (!prewarmStarted.get()) return
    if (!prewarmFinished.await(PREWARM_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LogUtil.w(TAG, "等 libmpv 预热超时，按未预热处理")
    }
}

/** 按应用版本分目录：升级换了 mediamp，旧版本的库不会被当成好的接着用。 */
private fun runtimeDir(): File =
    File(File(FileKit.filesDir.path, RUNTIME_DIR_NAME), BuildConfig.VERSION_NAME)

private fun loadRuntime(dir: File) {
    val marker = File(dir, MARKER_NAME)
    // 上次解到一半就崩了的话，目录里可能只有半个库，重来
    if (!marker.isFile) dir.deleteRecursively()
    try {
        MPVHandle.setRuntimeLibraryDirectory(dir.absolutePath, extractRuntimeLibrary = true)
    } catch (e: Throwable) {
        LogUtil.w(TAG, "libmpv 加载失败，清掉运行时目录重试一次", e)
        dir.deleteRecursively()
        MPVHandle.setRuntimeLibraryDirectory(dir.absolutePath, extractRuntimeLibrary = true)
    }
    marker.writeText(BuildConfig.VERSION_NAME)
    pruneOtherVersions(dir)
}

private fun pruneOtherVersions(current: File) {
    val root = current.parentFile ?: return
    root.listFiles()
        ?.filter { it.isDirectory && it.name != current.name }
        ?.forEach { stale ->
            if (stale.deleteRecursively()) LogUtil.d(TAG, "清掉旧版本的 libmpv：${stale.name}")
        }
}
