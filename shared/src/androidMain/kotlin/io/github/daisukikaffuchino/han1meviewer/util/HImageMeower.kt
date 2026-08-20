package io.github.daisukikaffuchino.han1meviewer.util

import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.ImageResult
import io.github.daisukikaffuchino.han1meviewer.logic.network.HDns
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.daisukikaffuchino.utils.applicationContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Suppress("NOTHING_TO_INLINE")
object HImageMeower {

    private const val TAG = "CoilImageNyanner"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .dns(HDns())
        .build()

    private val imageLoader = ImageLoader.Builder(applicationContext)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient })) }
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
