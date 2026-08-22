package io.github.daisukikaffuchino.han1meviewer.logic.platform

import io.github.daisukikaffuchino.han1meviewer.logic.model.HanimeVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// TODO(ios): 视频信息本地缓存尚未实现
private object NoopVideoCacheStore : VideoCacheStore {
    override fun load(videoCode: String): Flow<HanimeVideo?> = flowOf(null)
}

actual val platformVideoCacheStore: VideoCacheStore
    get() = NoopVideoCacheStore
