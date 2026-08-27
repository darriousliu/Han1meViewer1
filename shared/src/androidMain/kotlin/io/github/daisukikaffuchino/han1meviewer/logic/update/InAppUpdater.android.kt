package io.github.daisukikaffuchino.han1meviewer.logic.update

// Android 侧走应用商店/自行下载 APK 安装，暂不实现（真要做是 PackageInstaller 那一套，跟桌面完全无关）。
actual val supportsInAppUpdate: Boolean = false

actual suspend fun runInAppUpdate(onStage: (InAppUpdateStage) -> Unit): Result<Unit> =
    Result.failure(UnsupportedOperationException("in-app update not supported on this platform"))
