package com.example.pet.ui.chat

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.AttachmentKind
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import kotlin.math.roundToInt

private const val VIEWER_IMAGE_MAX_SIDE_PX = 2048
private const val ZOOM_MAX = 5f
private const val ZOOM_DOUBLE_TAP = 2.5f

@Composable
fun AttachmentViewerScreen(
    chatId: String,
    messageId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by remember(chatId) { AppContainer.chats.messages(chatId) }.collectAsStateWithLifecycle()
    val attachment = messages.firstOrNull { it.id == messageId }?.attachment
    if (attachment == null) {
        NotFoundScreen(title = stringResource(R.string.text_24_1), onBack = onBack, modifier = modifier)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = when (attachment.kind) {
                AttachmentKind.Image -> stringResource(R.string.text_10_16)
                AttachmentKind.Pdf -> attachment.name
            },
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (attachment.kind) {
                AttachmentKind.Image -> ZoomableImage(uri = attachment.uri)
                AttachmentKind.Pdf -> {
                    val file = remember(attachment.uri) { attachmentFile(attachment.uri) }
                    if (file != null) PdfPages(file = file) else ViewerError()
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(uri: String) {
    val image = rememberAttachmentImageState(uri, VIEWER_IMAGE_MAX_SIDE_PX)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Crossfade(targetState = image, label = "viewerImage") { current ->
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            when (current) {
                AttachmentImageState.Loading ->
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                AttachmentImageState.Failed -> ViewerError()
                is AttachmentImageState.Ready -> Image(
                    bitmap = current.bitmap,
                    contentDescription = stringResource(R.string.text_10_16),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = ZOOM_DOUBLE_TAP
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, ZOOM_MAX)
                                offset = if (scale == 1f) Offset.Zero else offset + pan
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )
            }
        }
    }
}

private sealed interface PdfState {
    data object Loading : PdfState
    data object Failed : PdfState
    class Ready(val document: PdfDocument) : PdfState
}

@Composable
private fun PdfPages(file: File) {
    val state by produceState<PdfState>(initialValue = PdfState.Loading, file) {
        val opened = withContext(NonCancellable + Dispatchers.IO) { runCatching { PdfDocument(file) }.getOrNull() }
        value = if (opened != null) PdfState.Ready(opened) else PdfState.Failed
        awaitDispose { opened?.close() }
    }

    when (val current = state) {
        PdfState.Loading -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
        }
        PdfState.Failed -> ViewerError()
        is PdfState.Ready -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val pageWidthPx = with(LocalDensity.current) { (maxWidth - 32.dp).roundToPx() }.coerceAtLeast(1)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outline)
            ) {
                items(current.document.pageSizes.size) { index ->
                    PdfPage(
                        document = current.document,
                        index = index,
                        size = current.document.pageSizes[index],
                        widthPx = pageWidthPx
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfPage(document: PdfDocument, index: Int, size: IntSize, widthPx: Int) {
    val ratio = if (size.width > 0 && size.height > 0) size.width.toFloat() / size.height else 1f
    val bitmap by produceState<ImageBitmap?>(initialValue = null, document, index, widthPx) {
        value = withContext(Dispatchers.IO) { document.render(index, widthPx)?.asImageBitmap() }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ViewerError() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.text_10_21),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private class PdfDocument(file: File) : Closeable {
    private val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = try {
        PdfRenderer(descriptor)
    } catch (error: Throwable) {
        descriptor.close()
        throw error
    }
    private val lock = Any()
    private var closed = false

    val pageSizes: List<IntSize> = try {
        (0 until renderer.pageCount).map { index ->
            renderer.openPage(index).use { page -> IntSize(page.width, page.height) }
        }
    } catch (error: Throwable) {
        renderer.close()
        descriptor.close()
        throw error
    }

    fun render(index: Int, widthPx: Int): Bitmap? = synchronized(lock) {
        if (closed) return null
        renderer.openPage(index).use { page ->
            val heightPx = (widthPx.toFloat() * page.height / page.width).roundToInt().coerceAtLeast(1)
            Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888).apply {
                eraseColor(android.graphics.Color.WHITE)
                page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }
        }
    }

    override fun close() {
        synchronized(lock) {
            if (!closed) {
                closed = true
                renderer.close()
                descriptor.close()
            }
        }
    }
}
