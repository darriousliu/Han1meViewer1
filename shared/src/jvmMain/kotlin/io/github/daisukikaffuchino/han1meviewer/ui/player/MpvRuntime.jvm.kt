package io.github.daisukikaffuchino.han1meviewer.ui.player

import io.github.daisukikaffuchino.utils.LogUtil
import org.openani.mediamp.mpv.MPVHandle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

private const val TAG = "MpvRuntime"

/**
 * Nucleus/Compose 打包时写进启动参数的应用资源目录。
 * `./gradlew run`、`runDistributable`、装好的应用三种形态都有，直接从 IDE 跑 main 没有。
 */
private const val APP_RESOURCES_DIR = "compose.application.resources.dir"

/** 预热卡死时别把播放页一起拖住，超时就照常往下走。 */
private const val PREWARM_TIMEOUT_SECONDS = 30L

private val prewarmStarted = AtomicBoolean(false)
private val prewarmFinished = CountDownLatch(1)

/**
 * 提前把 libmpv dlopen 好，在后台线程上做。
 *
 * 原生库由 `:desktopApp` 在**打包期**就摊进了应用资源目录（见那边的 `unpackMpvNatives`），
 * 所以运行时不解压、不落盘，这里只是把 mediamp 的运行时目录指过去并触发一次 `System.load`。
 *
 * 之所以还要提前做：mediamp 是懒加载的（`MpvMediampPlayer` 的构造函数就会碰 `handle`），
 * 不预热的话这次 dlopen 会落在播放页的 `remember { PlaybackEngineFactory.create(...) }` 里，
 * 也就是组合期，直接卡住 UI。
 *
 * 只在桌面端调，重复调用是空操作。
 */
fun prewarmMpvRuntime() {
    if (!prewarmStarted.compareAndSet(false, true)) return
    thread(name = "mpv-runtime-prewarm", isDaemon = true) {
        val startedAt = System.nanoTime()
        try {
            val dir = System.getProperty(APP_RESOURCES_DIR)
                ?: error(
                    "没有 $APP_RESOURCES_DIR：libmpv 在应用资源目录里，" +
                            "要用 ./gradlew run / runDistributable 或装好的应用启动"
                )
            MPVHandle.setRuntimeLibraryDirectory(dir, extractRuntimeLibrary = false)
            LogUtil.d(TAG, "libmpv 预热完成，耗时 ${(System.nanoTime() - startedAt) / 1_000_000}ms")
        } catch (e: Throwable) {
            // 预热失败不至于崩：PlaybackEngineFactory 会退化成恒为 Error 的空引擎，
            // 播放页照常渲染并给出错误信息
            LogUtil.e(TAG, "libmpv 预热失败，播放页会报播放内核初始化失败", e)
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
 * `already loaded from ... cannot be reconfigured` 挡下，播放页当场变成错误态。
 *
 * 正常情况下预热在启动时就跑完了，这里直接返回。
 */
internal fun awaitMpvRuntimePrewarm() {
    if (!prewarmStarted.get()) return
    if (!prewarmFinished.await(PREWARM_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LogUtil.w(TAG, "等 libmpv 预热超时，按未预热处理")
    }
}
