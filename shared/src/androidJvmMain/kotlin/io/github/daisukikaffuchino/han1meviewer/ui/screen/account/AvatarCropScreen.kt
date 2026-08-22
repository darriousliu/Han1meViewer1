package io.github.daisukikaffuchino.han1meviewer.ui.screen.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticButton as Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import cn.mucute.compose.avatar.cropper.AvatarCropper
import cn.mucute.compose.avatar.cropper.CropShape
import cn.mucute.compose.avatar.cropper.rememberCropState
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.confirm
import han1meviewer.shared.generated.resources.crop_avatar
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import org.jetbrains.compose.resources.decodeToImageBitmap

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AvatarCropScreen(
    sourceUri: String,
    onBack: () -> Unit,
    onConfirm: (path: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val cropState = rememberCropState()

    var originalImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(sourceUri) {
        val decoded = withContext(Dispatchers.IO) {
            runCatching { PlatformFile(sourceUri).readBytes().decodeToImageBitmap() }
                .onFailure { LogUtil.e("AvatarCrop", "解码失败: $sourceUri", it) }
                .getOrNull()
        }
        if (decoded == null) onBack() else originalImageBitmap = decoded
    }
    HanimeScaffold(
        title = stringResource(Res.string.crop_avatar),
        onBack = onBack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            originalImageBitmap?.let { bitmap ->
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarCropper(
                            imageBitmap = bitmap,
                            state = cropState,
                            shape = CropShape.Square,
                            modifier = Modifier.fillMaxSize(),
                            backgroundColor = MaterialTheme.colorScheme.background
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onBack,
                            enabled = !isProcessing
                        ) {
                            Text(stringResource(Res.string.cancel))
                        }

                        Button(
                            onClick = {
                                if (isProcessing) return@Button
                                isProcessing = true
                                scope.launch {
                                    val croppedResult = cropState.crop(bitmap)

                                    val path = withContext(Dispatchers.IO) {
                                        saveCroppedAvatar(croppedResult!!)
                                    }

                                    if (path != null) {
                                        onConfirm(path)
                                    } else {
                                        isProcessing = false
                                    }
                                }
                            },
                            enabled = !isProcessing
                        ) {
                            Text(stringResource(Res.string.confirm))
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

        }
    }
}

// 裁剪库 cn.mucute:compose-avatar-cropper 只发 android/jvm 变体，所以整页放在
// androidJvmMain；iOS 要么换 KMP 的裁剪库，要么把入口门控掉。
/** 把裁好的头像写进缓存目录，返回文件路径。 */
expect suspend fun saveCroppedAvatar(imageBitmap: ImageBitmap): String?
