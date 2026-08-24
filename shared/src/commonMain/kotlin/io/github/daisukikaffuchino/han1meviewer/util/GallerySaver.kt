package io.github.daisukikaffuchino.han1meviewer.util

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.saved
import io.github.daisukikaffuchino.han1meviewer.logic.network.ServiceCreator
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.vinceglb.filekit.FileKit
// 与下面那个 expect 同名，起个别名把两者分开
import io.github.vinceglb.filekit.saveImageToGallery as saveBytesToGallery
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/** 下载远程图片并保存到系统相册。 */
internal expect suspend fun saveImageToGallery(imageUrl: String)

/**
 * 桌面与 iOS 共用：FileKit 的 saveImageToGallery 在 iOS 上写进相册、
 * 在桌面上写进图片目录，Android 那边另有 MediaStore 的实现。
 */
internal suspend fun saveImageViaFileKit(imageUrl: String) {
    val bytes = withContext(Dispatchers.IO) {
        runCatching { ServiceCreator.imageClient.get(imageUrl).readRawBytes() }.getOrNull()
    } ?: return
    val saved = FileKit.saveBytesToGallery(
        bytes = bytes,
        filename = "IMG_${Clock.System.now().toEpochMilliseconds()}.jpg",
    )
    if (saved.isSuccess) {
        withContext(Dispatchers.Main) { SonnerToast.success(Res.string.saved) }
    }
}
