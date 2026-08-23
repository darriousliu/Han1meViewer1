package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.saved
import io.github.daisukikaffuchino.han1meviewer.logic.network.ServiceCreator
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/**
 * 桌面与 iOS 共用：FileKit 的 saveImageToGallery 在 iOS 上写进相册、
 * 在桌面上写进图片目录，Android 那边另有 MediaStore 的实现。
 */
internal suspend fun saveImageViaFileKit(imageUrl: String) {
    val bytes = withContext(Dispatchers.IO) {
        runCatching { ServiceCreator.imageClient.get(imageUrl).readRawBytes() }.getOrNull()
    } ?: return
    val saved = FileKit.saveImageToGallery(
        bytes = bytes,
        filename = "IMG_${Clock.System.now().toEpochMilliseconds()}.jpg",
    )
    if (saved.isSuccess) {
        withContext(Dispatchers.Main) { SonnerToast.success(Res.string.saved) }
    }
}
