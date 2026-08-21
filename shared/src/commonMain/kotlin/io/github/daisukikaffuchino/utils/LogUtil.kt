package io.github.daisukikaffuchino.utils

import co.touchlab.kermit.Logger
import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import kotlin.concurrent.Volatile

/** 门面保持原样，底下换成 Kermit，140 个调用点不用动。 */
object LogUtil {
    const val DEFAULT_TAG = "Han1meViewer"

    @Volatile
    var enabled: Boolean = BuildConfig.DEBUG

    fun v(message: String) = v(DEFAULT_TAG, message)

    fun v(tag: String, message: String) = log { Logger.withTag(tag).v { message } }

    fun d(message: String) = d(DEFAULT_TAG, message)

    fun d(tag: String, message: String) = log { Logger.withTag(tag).d { message } }

    fun i(message: String) = i(DEFAULT_TAG, message)

    fun i(tag: String, message: String) = log { Logger.withTag(tag).i { message } }

    fun w(message: String) = w(DEFAULT_TAG, message)

    fun w(message: String, throwable: Throwable?) = w(DEFAULT_TAG, message, throwable)

    fun w(tag: String, message: String) = log { Logger.withTag(tag).w { message } }

    fun w(tag: String, message: String, throwable: Throwable?) =
        log { Logger.withTag(tag).w(throwable) { message } }

    fun e(message: String) = e(DEFAULT_TAG, message)

    fun e(message: String, throwable: Throwable?) = e(DEFAULT_TAG, message, throwable)

    fun e(tag: String, message: String) = log { Logger.withTag(tag).e { message } }

    fun e(tag: String, message: String, throwable: Throwable?) =
        log { Logger.withTag(tag).e(throwable) { message } }

    private inline fun log(block: () -> Unit) {
        if (enabled) block()
    }
}
