package io.github.daisukikaffuchino.han1meviewer.ui.screen.account

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.write
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.time.Clock

actual suspend fun saveCroppedAvatar(imageBitmap: ImageBitmap): String? = runCatching {
    val data = Image.makeFromBitmap(imageBitmap.asSkiaBitmap())
        .encodeToData(EncodedImageFormat.JPEG, 90)
        ?: error("JPEG 编码失败")
    val file = FileKit.cacheDir / "avatar_${Clock.System.now().toEpochMilliseconds()}.jpg"
    file.write(data.bytes)
    file.absolutePath()
}.onFailure { LogUtil.e("AvatarCrop", "保存头像失败", it) }.getOrNull()
