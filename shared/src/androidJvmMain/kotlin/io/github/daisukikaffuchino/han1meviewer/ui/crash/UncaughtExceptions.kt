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
        if (previous != null) {
            // Android 上这是系统的 KillApplicationHandler，负责上报并结束进程
            previous.uncaughtException(thread, throwable)
        } else {
            // 桌面 JVM 默认没有注册 default handler，打印是 ThreadGroup 兜的；
            // 一旦我们设了 default handler 那条路就被绕开，不补这一下崩溃会完全静默，
            // 只剩一个退出码。不能转调 threadGroup.uncaughtException——它会回调
            // default handler，也就是我们自己，直接无限递归。
            System.err.print("Exception in thread \"${thread.name}\" ")
            throwable.printStackTrace()
        }
    }
}
