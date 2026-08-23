package io.github.daisukikaffuchino.han1meviewer.ui.screen.account

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.cancel
import han1meviewer.shared.generated.resources.confirm
import han1meviewer.shared.generated.resources.crop_avatar
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeScaffold
import io.github.daisukikaffuchino.han1meviewer.util.AVATAR_JPEG_QUALITY
import io.github.daisukikaffuchino.han1meviewer.util.AVATAR_MAX_DIMENSION
import io.github.daisukikaffuchino.han1meviewer.util.PREVIEW_MAX_DIMENSION
import io.github.daisukikaffuchino.han1meviewer.util.decodeSampledImageBitmap
import io.github.daisukikaffuchino.han1meviewer.util.encodeJpeg
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Clock
import androidx.compose.foundation.Canvas as DrawCanvas
import io.github.daisukikaffuchino.han1meviewer.ui.component.HapticButton as Button

/**
 * 头像裁剪页。取景框是自己画的：`cn.mucute:compose-avatar-cropper` 只发 android + jvm
 * 变体，进不了 commonMain。
 *
 * 大图安全性见 [decodeSampledImageBitmap]：解码上限 [PREVIEW_MAX_DIMENSION]，
 * 原图多大都不会整张按原分辨率进内存。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AvatarCropScreen(
    sourceUri: String,
    onBack: () -> Unit,
    onConfirm: (path: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val cropState = remember { SquareCropState() }

    var originalImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(sourceUri) {
        val decoded = runCatching { PlatformFile(sourceUri).readBytes() }
            .onFailure { LogUtil.e("AvatarCrop", "读不出来: $sourceUri", it) }
            .getOrNull()
            ?.let { decodeSampledImageBitmap(it, PREVIEW_MAX_DIMENSION) }
        // 读不出来/解不出来就直接退
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
                    SquareImageCropper(
                        image = bitmap,
                        state = cropState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

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
                                    val path = cropState.crop(bitmap, AVATAR_MAX_DIMENSION)
                                        ?.let { saveCroppedAvatar(it) }
                                    if (path != null) onConfirm(path) else isProcessing = false
                                }
                            },
                            enabled = !isProcessing
                        ) {
                            Text(stringResource(Res.string.confirm))
                        }
                    }
                }
            } ?: LoadingIndicator()
        }
    }
}

/**
 * 裁好的头像写进缓存目录，返回文件路径。
 * 上传侧要的是 PlatformFile，所以这里落一次盘，不直接传字节。
 */
private suspend fun saveCroppedAvatar(bitmap: ImageBitmap): String? {
    val jpeg = encodeJpeg(bitmap, AVATAR_JPEG_QUALITY) ?: return null
    return runCatching {
        val file = FileKit.cacheDir / "avatar_${Clock.System.now().toEpochMilliseconds()}.jpg"
        file.write(jpeg)
        file.path
    }.onFailure { LogUtil.e("AvatarCrop", "保存头像失败", it) }.getOrNull()
}

/** 取景框占容器短边的比例，留点边好让用户看清框外会被裁掉的部分。 */
private const val VIEWPORT_FRACTION = 0.8f

private const val MAX_ZOOM = 5f

/**
 * [SquareImageCropper] 的取景状态。
 *
 * 只有缩放、偏移、容器尺寸三个原始值是可变状态，取景框边长、基础缩放、钳过的偏移
 * 全是按需算的 —— 组合阶段不往状态里写东西，不会出现「写了自己读的状态 → 无限重组」。
 *
 * 源图尺寸不存状态而是每次当参数传：存的话首帧拿不到（要等 LaunchedEffect 回填），
 * 那一帧会按 scale=1 把原图整张画出来，闪一下。
 */
class SquareCropState {

    private var rawZoom by mutableFloatStateOf(1f)
    private var rawOffset by mutableStateOf(Offset.Zero)

    /** 容器像素尺寸，由 onSizeChanged（布局阶段）回填 */
    internal var containerSize by mutableStateOf(IntSize.Zero)

    /** 取景框边长（像素） */
    internal val viewport: Float
        get() = min(containerSize.width, containerSize.height) * VIEWPORT_FRACTION

    /** 图片刚好盖满取景框时的缩放：短边铺满，长边溢出，正好留出可拖的余量 */
    internal fun baseScale(imageWidth: Int, imageHeight: Int): Float {
        val shortest = min(imageWidth, imageHeight)
        return if (shortest <= 0) 1f else viewport / shortest
    }

    internal fun totalScale(imageWidth: Int, imageHeight: Int): Float =
        baseScale(imageWidth, imageHeight) * rawZoom

    /** 钳过的偏移：保证取景框始终落在图片内，拖不出白边 */
    internal fun offset(imageWidth: Int, imageHeight: Int): Offset {
        val scale = totalScale(imageWidth, imageHeight)
        val maxX = max(0f, (imageWidth * scale - viewport) / 2f)
        val maxY = max(0f, (imageHeight * scale - viewport) / 2f)
        return Offset(
            rawOffset.x.coerceIn(-maxX, maxX),
            rawOffset.y.coerceIn(-maxY, maxY),
        )
    }

    internal fun onTransform(
        pan: Offset,
        gestureZoom: Float,
        imageWidth: Int,
        imageHeight: Int,
    ) {
        rawZoom = (rawZoom * gestureZoom).coerceIn(1f, MAX_ZOOM)
        // 从钳过的值上叠加，否则拖到边界后会攒出一堆无效偏移，回拖时要空走一段
        rawOffset = offset(imageWidth, imageHeight) + pan
    }

    fun reset() {
        rawZoom = 1f
        rawOffset = Offset.Zero
    }

    /**
     * 把取景框里的内容裁成边长不超过 [maxSize] 的方图。
     *
     * 屏幕坐标反算源图坐标：图片以中心对齐画出，实际缩放是 [totalScale]，
     * 所以取景框边长对应源图 `viewport / totalScale`，中心由 [offset] 反推。
     */
    fun crop(image: ImageBitmap, maxSize: Int): ImageBitmap? {
        val scale = totalScale(image.width, image.height)
        if (viewport <= 0f || scale <= 0f) return null

        val offset = offset(image.width, image.height)
        val srcSide = viewport / scale
        val srcCenterX = image.width / 2f - offset.x / scale
        val srcCenterY = image.height / 2f - offset.y / scale

        // 浮点误差可能让边框差几个像素越界，统一钳进图内
        val side = srcSide.roundToInt().coerceIn(1, min(image.width, image.height))
        val left = (srcCenterX - side / 2f).roundToInt().coerceIn(0, image.width - side)
        val top = (srcCenterY - side / 2f).roundToInt().coerceIn(0, image.height - side)

        val outSize = min(side, maxSize)
        return runCatching {
            val output = ImageBitmap(outSize, outSize)
            Canvas(output).drawImageRect(
                image = image,
                srcOffset = IntOffset(left, top),
                srcSize = IntSize(side, side),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(outSize, outSize),
                paint = Paint().apply {
                    isAntiAlias = true
                    filterQuality = FilterQuality.High
                },
            )
            output
        }.getOrNull()
    }
}

/**
 * 方形取景框：图片可拖动、双指缩放、双击复位，取景框固定在正中，框外压暗。
 * 平移和缩放都受 [SquareCropState] 约束，取景框永远落在图片内，裁出来不会带白边。
 */
@Composable
fun SquareImageCropper(
    image: ImageBitmap,
    state: SquareCropState,
    modifier: Modifier = Modifier,
) {
    // 换图（理论上本页不会）时把取景归位
    LaunchedEffect(image) { state.reset() }

    Box(
        modifier = modifier
            .clipToBounds()
            .background(Color.Black)
            .onSizeChanged { state.containerSize = it }
            .pointerInput(image) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    state.onTransform(pan, gestureZoom, image.width, image.height)
                }
            }
            .pointerInput(image) {
                detectTapGestures(onDoubleTap = { state.reset() })
            },
    ) {
        DrawCanvas(modifier = Modifier.fillMaxSize()) {
            val viewport = state.viewport
            if (viewport <= 0f) return@DrawCanvas

            val scale = state.totalScale(image.width, image.height)
            val offset = state.offset(image.width, image.height)
            val drawWidth = image.width * scale
            val drawHeight = image.height * scale
            val left = size.width / 2f - drawWidth / 2f + offset.x
            val top = size.height / 2f - drawHeight / 2f + offset.y

            drawImage(
                image = image,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(image.width, image.height),
                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                dstSize = IntSize(drawWidth.roundToInt(), drawHeight.roundToInt()),
                filterQuality = FilterQuality.Medium,
            )

            // 框外压暗：clipOp = Difference 把取景框那块挖掉，只画外面
            val frameLeft = size.width / 2f - viewport / 2f
            val frameTop = size.height / 2f - viewport / 2f
            clipRect(
                left = frameLeft,
                top = frameTop,
                right = frameLeft + viewport,
                bottom = frameTop + viewport,
                clipOp = ClipOp.Difference,
            ) {
                drawRect(color = Color.Black.copy(alpha = 0.6f))
            }
            drawRect(
                color = Color.White,
                topLeft = Offset(frameLeft, frameTop),
                size = Size(viewport, viewport),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
