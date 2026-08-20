package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.preview.getchupreview

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.daisukikaffuchino.han1meviewer.logic.network.ServiceCreator
import coil3.request.ImageRequest
import java.time.LocalDate

internal fun currentGetchuDateCode(): String {
    val now = LocalDate.now()
    return "%04d%02d".format(now.year, now.monthValue)
}

internal fun shiftGetchuMonthCode(code: String, delta: Int): String {
    var year = code.substring(0, 4).toInt()
    var month = code.substring(4, 6).toInt() + delta
    while (month < 1) {
        month += 12
        year -= 1
    }
    while (month > 12) {
        month -= 12
        year += 1
    }
    return "%04d%02d".format(year, month)
}

internal fun getchuDateLabel(code: String): String {
    return "${code.substring(0, 4)}/${code.substring(4, 6).toInt()}"
}

internal fun getchuMonthOptions(centerCode: String): List<String> {
    return (-12..12).map { delta -> shiftGetchuMonthCode(centerCode, delta) }
}

@Composable
internal fun getchuImageRequest(url: String?): ImageRequest {
    val context = LocalContext.current
    return ImageRequest.Builder(context)
        .data(url)
        .build()
}

@Composable
internal fun rememberGetchuImageLoader(): ImageLoader {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    return remember(context, isInspectionMode) {
        if (isInspectionMode) {
            ImageLoader.Builder(context).build()
        } else {
            createGetchuImageLoader(context)
        }
    }
}

internal fun createGetchuImageLoader(context: Context): ImageLoader =
    ImageLoader.Builder(context)
        // getchu 图要的 UA/Referer/Cookie 与 DNS、代理都在 getchuClient 上了，不用再配一遍
        .components { add(KtorNetworkFetcherFactory(httpClient = { ServiceCreator.getchuClient })) }
        .build()
