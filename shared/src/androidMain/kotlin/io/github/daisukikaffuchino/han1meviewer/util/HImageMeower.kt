package io.github.daisukikaffuchino.han1meviewer.util

import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.ImageResult
import io.github.daisukikaffuchino.han1meviewer.logic.network.ServiceCreator
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.daisukikaffuchino.utils.applicationContext

@Suppress("NOTHING_TO_INLINE")
object HImageMeower {

    private const val TAG = "CoilImageNyanner"

    private val imageLoader = ImageLoader.Builder(applicationContext)
        // 传 lambda 而不是实例：切镜像/代理/DNS 后 client 会重建，这样能自动取到新的
        .components { add(KtorNetworkFetcherFactory(httpClient = { ServiceCreator.hClient })) }
        .build()

    suspend fun execute(data: Any): ImageResult {
        LogUtil.d(TAG, "execute: $data")
        return imageLoader.execute(
            ImageRequest.Builder(applicationContext).data(data).build()
        )
    }

    inline fun placeholder(height: Int, width: Int, blur: Int = 8) =
        "https://picsum.photos/$width/$height/?blur=$blur"
}
