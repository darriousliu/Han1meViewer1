package io.github.daisukikaffuchino.utils

import han1meviewer.shared.generated.resources.Res
import io.github.daisukikaffuchino.han1meviewer.HJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@PublishedApi
internal val assetCache: MutableMap<String, Any> = mutableMapOf()

@PublishedApi
internal val assetMutex: Mutex = Mutex()

@PublishedApi
internal suspend fun readAssetText(filePath: String): String =
    Res.readBytes("files/$filePath").decodeToString()

/**
 * 读 composeResources/files 下的 json 并缓存，失败返回 null（与原 assets 版一致）。
 */
suspend inline fun <reified T : Any> loadAssetAs(filePath: String): T? =
    assetMutex.withLock {
        @Suppress("UNCHECKED_CAST")
        (assetCache[filePath] as? T) ?: runCatching {
            HJson.decodeFromString<T>(readAssetText(filePath))
        }.getOrNull()?.also { assetCache[filePath] = it }
    }
