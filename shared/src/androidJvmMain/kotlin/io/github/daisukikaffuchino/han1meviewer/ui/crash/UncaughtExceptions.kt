package io.github.daisukikaffuchino.han1meviewer.ui.crash

/**
 * 装一个未捕获异常钩子。Thread.setDefaultUncaughtExceptionHandler 是 JDK API，
 * Android 和桌面端通用；处理完仍交回原来的处理器，不吞掉链路上别人的逻辑。
 *
 * onCrash 里抛出的异常会被吞掉——崩溃处理本身再崩会盖掉原始堆栈。
 */
fun installUncaughtExceptionHandler(onCrash: (Throwable) -> Unit) {
    val previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        runCatching { onCrash(throwable) }
        previous?.uncaughtException(thread, throwable)
    }
}
