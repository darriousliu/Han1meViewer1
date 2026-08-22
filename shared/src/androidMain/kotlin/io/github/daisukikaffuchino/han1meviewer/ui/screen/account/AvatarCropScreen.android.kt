package io.github.daisukikaffuchino.han1meviewer.ui.screen.account

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.daisukikaffuchino.utils.applicationContext
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Clock

actual suspend fun saveCroppedAvatar(imageBitmap: ImageBitmap): String? = runCatching {
    val file = File(
        applicationContext.cacheDir,
        "avatar_${Clock.System.now().toEpochMilliseconds()}.jpg",
    )
    FileOutputStream(file).use { out ->
        imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
    }
    file.absolutePath
}.onFailure { LogUtil.e("AvatarCrop", "保存头像失败", it) }.getOrNull()
