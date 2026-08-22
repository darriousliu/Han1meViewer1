package io.github.daisukikaffuchino.han1meviewer.ui.crash

actual fun crashReportPlatformInfo(): List<String> = listOf(
    "OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} " +
            "(${System.getProperty("os.arch")})",
    "JVM: ${System.getProperty("java.vendor")} ${System.getProperty("java.version")}",
)
