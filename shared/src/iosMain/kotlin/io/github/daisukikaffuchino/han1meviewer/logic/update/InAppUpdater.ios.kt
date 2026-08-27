package io.github.daisukikaffuchino.han1meviewer.logic.update

// iOS 不存在应用内安装这回事，只能去 App Store 或侧载。
actual val supportsInAppUpdate: Boolean = false

actual suspend fun runInAppUpdate(onStage: (InAppUpdateStage) -> Unit): Result<Unit> =
    Result.failure(UnsupportedOperationException("in-app update not supported on this platform"))
