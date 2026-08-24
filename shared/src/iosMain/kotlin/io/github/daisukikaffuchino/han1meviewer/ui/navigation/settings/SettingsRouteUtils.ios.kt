package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import platform.AVKit.AVPictureInPictureController

// iPad 上分屏时会返回 false
internal actual val isPipModeSupported: Boolean =
    AVPictureInPictureController.isPictureInPictureSupported()

// iOS 的画中画不需要单独授权，能不能用就看 isPipModeSupported
internal actual fun isPipPermissionGranted(): Boolean = true

internal actual fun openPipPermissionSettings() = Unit