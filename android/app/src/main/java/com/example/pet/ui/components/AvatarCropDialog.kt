package com.example.pet.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pet.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun AvatarCropDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onCropped: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { decodeBitmap(context, uri, 2048) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val cropSize = min(widthPx, heightPx) - with(density) { 48.dp.toPx() }
            val center = Offset(widthPx / 2f, heightPx / 2f)

            var zoom by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            val bmp = bitmap
            val minScale = if (bmp != null) max(cropSize / bmp.width, cropSize / bmp.height) else 1f

            fun clampOffset(o: Offset, z: Float): Offset {
                if (bmp == null) return Offset.Zero
                val s = minScale * z
                val maxX = max(0f, (bmp.width * s - cropSize) / 2f)
                val maxY = max(0f, (bmp.height * s - cropSize) / 2f)
                return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
            }

            if (bmp != null) {
                val imageBitmap = remember(bmp) { bmp.asImageBitmap() }
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(bmp, cropSize) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                val newZoom = (zoom * gestureZoom).coerceIn(1f, 5f)
                                zoom = newZoom
                                offset = clampOffset(offset + pan, newZoom)
                            }
                        }
                ) {
                    val s = minScale * zoom
                    val dw = (bmp.width * s).roundToInt()
                    val dh = (bmp.height * s).roundToInt()
                    drawImage(
                        image = imageBitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bmp.width, bmp.height),
                        dstOffset = IntOffset(
                            (center.x - dw / 2f + offset.x).roundToInt(),
                            (center.y - dh / 2f + offset.y).roundToInt()
                        ),
                        dstSize = IntSize(dw, dh)
                    )
                    val dim = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(Rect(Offset.Zero, size))
                        addOval(Rect(center, cropSize / 2f))
                    }
                    drawPath(dim, Color.Black.copy(alpha = 0.6f))
                    drawCircle(
                        color = Color.White,
                        radius = cropSize / 2f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.text_4_8),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                TextButton(
                    enabled = bmp != null,
                    onClick = {
                        val source = bmp ?: return@TextButton
                        val z = zoom
                        val o = offset
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                val s = minScale * z
                                val side = min(
                                    (cropSize / s).roundToInt(),
                                    min(source.width, source.height)
                                ).coerceAtLeast(1)
                                val x = ((source.width * s / 2f - cropSize / 2f - o.x) / s)
                                    .roundToInt().coerceIn(0, source.width - side)
                                val y = ((source.height * s / 2f - cropSize / 2f - o.y) / s)
                                    .roundToInt().coerceIn(0, source.height - side)
                                val cropped = Bitmap.createBitmap(source, x, y, side, side)
                                val finalBitmap = if (side > 512) {
                                    Bitmap.createScaledBitmap(cropped, 512, 512, true)
                                } else cropped
                                val file = File(
                                    context.cacheDir,
                                    "pet_avatar_${System.currentTimeMillis()}.jpg"
                                )
                                FileOutputStream(file).use {
                                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                                }
                                Uri.fromFile(file)
                            }
                            onCropped(result)
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.text_4_9),
                        color = if (bmp != null) Color.White else Color.Gray,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

private fun decodeBitmap(context: Context, uri: Uri, maxSide: Int): Bitmap? {
    return runCatching {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return@runCatching null

        val rotation = resolver.openInputStream(uri)?.use {
            when (
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        if (rotation == 0f) {
            bitmap
        } else {
            Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height,
                Matrix().apply { postRotate(rotation) }, true
            )
        }
    }.getOrNull()
}