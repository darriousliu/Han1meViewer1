package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.saved
import io.github.daisukikaffuchino.utils.SonnerToast
import io.github.daisukikaffuchino.utils.applicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

internal actual suspend fun saveImageToGallery(imageUrl: String) {
    val context = applicationContext
    val loader = SingletonImageLoader.get(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .build()
    val result = (loader.execute(request) as? SuccessResult)?.image
    val bitmap = result?.toBitmap() ?: return
    val filename = "IMG_${Clock.System.now().toEpochMilliseconds()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    val fos = uri?.let { context.contentResolver.openOutputStream(it) }
    fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    withContext(Dispatchers.Main) {
        SonnerToast.success(Res.string.saved)
    }
}
