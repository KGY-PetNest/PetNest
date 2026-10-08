package com.example.pet.ui.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import com.example.pet.R
import com.example.pet.data.AttachmentKind
import com.example.pet.data.ChatAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

const val ATTACHMENT_MAX_MB = 20
const val ATTACHMENTS_PER_MESSAGE_MAX = 10
private const val ATTACHMENT_MAX_BYTES = ATTACHMENT_MAX_MB * 1024L * 1024L
private const val ATTACHMENT_DIR = "chat_files"
private const val IMAGE_MAX_SIDE_PX = 2048
private const val JPEG_QUALITY = 85
private const val FILE_NAME_MAX_LENGTH = 100
private const val IMAGE_CACHE_BYTES = 32 * 1024 * 1024
private val PdfSignature = byteArrayOf(0x25, 0x50, 0x44, 0x46)

class AttachmentTooLargeException : IllegalArgumentException()

class AttachmentUnsupportedException : IllegalArgumentException()

@StringRes
fun attachmentErrorText(error: Throwable): Int = when (error) {
    is AttachmentTooLargeException -> R.string.text_10_20
    else -> R.string.text_10_21
}

fun attachmentFile(uri: String): File? = Uri.parse(uri).path?.let(::File)

suspend fun importImageAttachment(context: Context, uri: Uri): Result<ChatAttachment> = withContext(Dispatchers.IO) {
    runCatching {
        val bitmap = decodeImage(context, uri, IMAGE_MAX_SIDE_PX) ?: throw AttachmentUnsupportedException()
        val png = bitmap.hasAlpha()
        val file = newAttachmentFile(context, if (png) "png" else "jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(
                    if (png) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                    JPEG_QUALITY,
                    out
                )
            }
            if (file.length() > ATTACHMENT_MAX_BYTES) throw AttachmentTooLargeException()
            ChatAttachment(
                kind = AttachmentKind.Image,
                uri = Uri.fromFile(file).toString(),
                name = file.name,
                sizeBytes = file.length(),
                width = bitmap.width,
                height = bitmap.height
            )
        } catch (error: Throwable) {
            file.delete()
            throw error
        }
    }
}

suspend fun importPdfAttachment(context: Context, uri: Uri): Result<ChatAttachment> = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val (displayName, declaredSize) = queryNameAndSize(context, uri)
        if (declaredSize != null && declaredSize > ATTACHMENT_MAX_BYTES) throw AttachmentTooLargeException()
        val file = newAttachmentFile(context, "pdf")
        try {
            val input = resolver.openInputStream(uri) ?: throw AttachmentUnsupportedException()
            input.use { source ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var copied = 0L
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        copied += read
                        if (copied > ATTACHMENT_MAX_BYTES) throw AttachmentTooLargeException()
                        output.write(buffer, 0, read)
                    }
                }
            }
            if (!hasPdfSignature(file)) throw AttachmentUnsupportedException()
            ChatAttachment(
                kind = AttachmentKind.Pdf,
                uri = Uri.fromFile(file).toString(),
                name = displayName?.take(FILE_NAME_MAX_LENGTH)?.ifBlank { null } ?: file.name,
                sizeBytes = file.length()
            )
        } catch (error: Throwable) {
            file.delete()
            throw error
        }
    }
}

@Composable
fun fileSizeText(bytes: Long): String {
    val mb = 1024L * 1024L
    return if (bytes < mb) {
        stringResource(R.string.common_size_kb, (bytes / 1024L).coerceAtLeast(1L).toInt())
    } else {
        stringResource(R.string.common_size_mb, bytes.toFloat() / mb)
    }
}

sealed interface AttachmentImageState {
    data object Loading : AttachmentImageState
    data object Failed : AttachmentImageState
    class Ready(val bitmap: ImageBitmap) : AttachmentImageState
}

@Composable
fun rememberAttachmentImageState(uri: String, maxSidePx: Int): AttachmentImageState {
    val key = "$uri@$maxSidePx"
    val cached = remember(key) {
        ImageCache.get(key)?.let { AttachmentImageState.Ready(it.asImageBitmap()) } ?: AttachmentImageState.Loading
    }
    val state by produceState(initialValue = cached, key) {
        val hit = ImageCache.get(key)
        if (hit != null) {
            value = AttachmentImageState.Ready(hit.asImageBitmap())
            return@produceState
        }
        value = AttachmentImageState.Loading
        val bitmap = withContext(Dispatchers.IO) {
            loadScaled(uri, maxSidePx)?.also { ImageCache.put(key, it) }
        }
        value = if (bitmap != null) AttachmentImageState.Ready(bitmap.asImageBitmap()) else AttachmentImageState.Failed
    }
    return state
}

@Composable
fun rememberAttachmentImage(uri: String, maxSidePx: Int): ImageBitmap? =
    (rememberAttachmentImageState(uri, maxSidePx) as? AttachmentImageState.Ready)?.bitmap

object ChatDrafts {
    private val pending = HashMap<String, List<ChatAttachment>>()

    fun pending(chatId: String): List<ChatAttachment> = synchronized(pending) { pending[chatId].orEmpty() }

    fun setPending(chatId: String, attachments: List<ChatAttachment>) {
        synchronized(pending) {
            if (attachments.isEmpty()) pending.remove(chatId) else pending[chatId] = attachments
        }
    }

    fun clear() {
        val all = synchronized(pending) {
            val values = pending.values.flatten()
            pending.clear()
            values
        }
        all.forEach { attachmentFile(it.uri)?.delete() }
    }
}

private val ImageCache = object : LruCache<String, Bitmap>(IMAGE_CACHE_BYTES) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}

private fun loadScaled(uri: String, maxSide: Int): Bitmap? = runCatching {
    val path = Uri.parse(uri).path ?: return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxSide && bounds.outHeight / (sample * 2) >= maxSide) {
        sample *= 2
    }
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}.getOrNull()

private fun newAttachmentFile(context: Context, extension: String): File {
    val dir = File(context.filesDir, ATTACHMENT_DIR).apply { mkdirs() }
    return File(dir, "${UUID.randomUUID()}.$extension")
}

private fun hasPdfSignature(file: File): Boolean = runCatching {
    file.inputStream().use { input ->
        val head = ByteArray(PdfSignature.size)
        input.read(head) == head.size && head.contentEquals(PdfSignature)
    }
}.getOrDefault(false)

private fun queryNameAndSize(context: Context, uri: Uri): Pair<String?, Long?> = runCatching {
    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
        null,
        null,
        null
    )?.use { cursor ->
        if (!cursor.moveToFirst()) return@use (null to null)
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        val name = if (nameIndex >= 0 && !cursor.isNull(nameIndex)) cursor.getString(nameIndex) else null
        val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
        name to size
    } ?: (null to null)
}.getOrDefault(null to null)

private fun decodeImage(context: Context, uri: Uri, maxSide: Int): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val width = info.size.width
            val height = info.size.height
            val scale = maxSide.toFloat() / max(width, height)
            if (scale < 1f) {
                decoder.setTargetSize(
                    (width * scale).roundToInt().coerceAtLeast(1),
                    (height * scale).roundToInt().coerceAtLeast(1)
                )
            }
        }
    } else {
        decodeLegacy(context, uri, maxSide)
    }
}.getOrNull()

private fun decodeLegacy(context: Context, uri: Uri, maxSide: Int): Bitmap? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (bounds.outWidth / sample > maxSide * 2 || bounds.outHeight / sample > maxSide * 2) {
        sample *= 2
    }
    val decoded = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return null

    val rotation = resolver.openInputStream(uri)?.use {
        when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f

    val scale = minOf(1f, maxSide.toFloat() / max(decoded.width, decoded.height))
    if (rotation == 0f && scale == 1f) return decoded
    val matrix = Matrix().apply {
        postScale(scale, scale)
        postRotate(rotation)
    }
    return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
}
