package io.github.daisukikaffuchino.han1meviewer.logic.platform

actual val platformVideoCacheStore: VideoCacheStore
    get() = FileVideoCacheStore
