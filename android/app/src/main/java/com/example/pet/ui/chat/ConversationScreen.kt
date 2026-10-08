package com.example.pet.ui.chat

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.AttachmentKind
import com.example.pet.data.Chat
import com.example.pet.data.ChatAttachment
import com.example.pet.data.ChatMessage
import com.example.pet.data.MessageStatus
import com.example.pet.data.canChat
import com.example.pet.data.UserRole
import com.example.pet.notifications.AppForeground
import com.example.pet.notifications.ChatNotifier
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.NotificationPermissionPrompt
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

private const val GROUP_GAP_MINUTES = 5L
private const val BUBBLE_WIDTH_FRACTION = 0.78f
private const val ERROR_VISIBLE_MS = 3500L
private const val IMAGE_RATIO_MIN = 0.6f
private const val IMAGE_RATIO_MAX = 1.8f
private val BubbleMaxWidth = 480.dp
private val BubbleImageMaxWidth = 260.dp
private val BubbleRadius = 18.dp
private val BubbleTailRadius = 4.dp
private val PendingTileSize = 64.dp

private sealed interface ChatEntry {
    val key: String
}

private data class DayEntry(val date: LocalDate) : ChatEntry {
    override val key: String get() = "day-$date"
}

private data class MessageEntry(
    val message: ChatMessage,
    val groupStart: Boolean,
    val groupEnd: Boolean
) : ChatEntry {
    override val key: String get() = message.id
}

@Composable
fun ConversationScreen(
    chatId: String,
    role: UserRole,
    onBack: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenAttachment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chats by remember(role) { AppContainer.chats.chats(role) }.collectAsStateWithLifecycle()
    val messages by remember(chatId) { AppContainer.chats.messages(chatId) }.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var draft by rememberSaveable { mutableStateOf("") }
    var pending by remember { mutableStateOf(ChatDrafts.pending(chatId)) }
    val feed by AppContainer.requests.feed.collectAsStateWithLifecycle()
    val responded by AppContainer.requests.respondedIds.collectAsStateWithLifecycle()
    var importing by remember { mutableStateOf(false) }
    var attachSheetOpen by remember { mutableStateOf(false) }
    var errorRes by remember { mutableStateOf<Int?>(null) }
    val entries = remember(messages) { buildEntries(messages) }
    val lastMessage = messages.lastOrNull()
    val safety = rememberChatSafetyState()

    NotificationPermissionPrompt()

    DisposableEffect(chatId) {
        AppForeground.openChatId = chatId
        ChatNotifier.cancel(context, chatId)
        onDispose {
            if (AppForeground.openChatId == chatId) AppForeground.openChatId = null
        }
    }

    LaunchedEffect(lastMessage?.id) {
        if (lastMessage != null && (lastMessage.senderRole == role || listState.firstVisibleItemIndex <= 2)) {
            listState.animateScrollToItem(0)
        }
        AppContainer.chats.markRead(chatId)
        ChatNotifier.cancel(context, chatId)
    }

    LaunchedEffect(errorRes) {
        if (errorRes != null) {
            delay(ERROR_VISIBLE_MS)
            errorRes = null
        }
    }

    LaunchedEffect(pending) { ChatDrafts.setPending(chatId, pending) }

    fun importFiles(uris: List<Uri>, importer: suspend (Context, Uri) -> Result<ChatAttachment>) {
        val room = ATTACHMENTS_PER_MESSAGE_MAX - pending.size
        if (uris.isEmpty() || room <= 0) return
        scope.launch {
            importing = true
            uris.take(room).forEach { uri ->
                importer(context, uri)
                    .onSuccess { pending = pending + it }
                    .onFailure { errorRes = attachmentErrorText(it) }
            }
            importing = false
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ATTACHMENTS_PER_MESSAGE_MAX)
    ) { uris -> importFiles(uris, ::importImageAttachment) }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importFiles(listOf(uri), ::importPdfAttachment)
    }

    val chat = chats.firstOrNull { it.id == chatId }
    if (chat == null) {
        NotFoundScreen(title = stringResource(R.string.text_10_1), onBack = onBack, modifier = modifier)
        return
    }
    val canWrite = role == UserRole.Owner ||
            feed.firstOrNull { it.id == chat.requestId }?.statusFor(chat.volunteerId, responded).canChat

    fun send() {
        val text = draft.trim()
        val files = pending
        if (text.isEmpty() && files.isEmpty()) return
        draft = ""
        pending = emptyList()
        scope.launch {
            if (files.isEmpty()) {
                AppContainer.chats.send(chatId, role, text)
            } else {
                files.forEachIndexed { index, file ->
                    AppContainer.chats.send(chatId, role, if (index == 0) text else "", file)
                }
            }
        }
    }

    if (attachSheetOpen) {
        AttachSheet(
            onPickPhotos = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onPickPdf = { pdfPicker.launch(arrayOf("application/pdf")) },
            onDismiss = { attachSheetOpen = false }
        )
    }

    ChatSafetyDialogs(state = safety, chat = chat)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
        ) {
            ConversationHeader(
                chat = chat,
                onBack = onBack,
                onOpenInfo = onOpenInfo,
                onReport = { safety.reportOpen = true },
                onToggleBlock = { safety.blockDialogOpen = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clearFocusOnTap()
            ) {
                if (entries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.text_10_11),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp)
                    )
                }
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.key }, contentType = { it::class }) { entry ->
                        when (entry) {
                            is DayEntry -> DayChip(
                                date = entry.date,
                                modifier = Modifier.animateItem()
                            )
                            is MessageEntry -> {
                                val event = entry.message.event
                                if (event != null) {
                                    EventChip(
                                        text = chatEventText(event, role),
                                        modifier = Modifier.animateItem()
                                    )
                                } else MessageBubble(
                                    message = entry.message,
                                    mine = entry.message.senderRole == role,
                                    groupStart = entry.groupStart,
                                    groupEnd = entry.groupEnd,
                                    onRetry = {
                                        scope.launch { AppContainer.chats.resend(chatId, entry.message.id) }
                                    },
                                    onOpenAttachment = { onOpenAttachment(entry.message.id) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            if (chat.blocked) {
                BlockedBanner(onUnblock = { safety.blockDialogOpen = true })
            } else if (!canWrite) {
                Text(
                    text = stringResource(R.string.text_10_56),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
            } else {
                AnimatedVisibility(
                    visible = errorRes != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ErrorLine(errorRes)
                }

                AnimatedVisibility(
                    visible = pending.isNotEmpty() || importing,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PendingAttachments(
                        attachments = pending,
                        importing = importing,
                        onRemove = { attachment ->
                            pending = pending - attachment
                            attachmentFile(attachment.uri)?.delete()
                        }
                    )
                }

                MessageInput(
                    value = draft,
                    canSend = draft.isNotBlank() || pending.isNotEmpty(),
                    canAttach = !importing && pending.size < ATTACHMENTS_PER_MESSAGE_MAX,
                    onValueChange = { draft = it.take(FormRules.LONG_TEXT_MAX_LENGTH) },
                    onAttach = { attachSheetOpen = true },
                    onSend = { send() }
                )
            }
        }
    }
}

@Composable
private fun ConversationHeader(
    chat: Chat,
    onBack: () -> Unit,
    onOpenInfo: () -> Unit,
    onReport: () -> Unit,
    onToggleBlock: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 4.dp, end = 4.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenInfo)
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            InitialsAvatar(name = chat.companionName, photoUri = chat.companionAvatarUri, size = 42.dp)
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = chat.companionName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.text_10_5, chat.requestTitle, chat.requestDates),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.text_10_53)
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.text_10_32)) },
                    leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onReport()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(if (chat.blocked) R.string.text_10_34 else R.string.text_10_33),
                            color = if (chat.blocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = if (chat.blocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        menuOpen = false
                        onToggleBlock()
                    }
                )
            }
        }
    }
}

@Composable
private fun EventChip(text: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DayChip(date: LocalDate, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = chatDayLabel(date),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    mine: Boolean,
    groupStart: Boolean,
    groupEnd: Boolean,
    onRetry: () -> Unit,
    onOpenAttachment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxWidth = (screenWidth * BUBBLE_WIDTH_FRACTION).coerceAtMost(BubbleMaxWidth)
    val imageWidth = maxWidth.coerceAtMost(BubbleImageMaxWidth)
    val failed = message.status == MessageStatus.Failed
    val attachment = message.attachment
    val hasImage = attachment?.kind == AttachmentKind.Image
    val container = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val content = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val textStart = if (hasImage) 8.dp else 12.dp
    val textEnd = if (hasImage) 6.dp else 10.dp
    val senderCorner = if (groupStart) BubbleRadius else BubbleTailRadius
    val shape = if (mine) {
        RoundedCornerShape(
            topStart = BubbleRadius,
            topEnd = senderCorner,
            bottomEnd = if (groupEnd) BubbleTailRadius else senderCorner,
            bottomStart = BubbleRadius
        )
    } else {
        RoundedCornerShape(
            topStart = senderCorner,
            topEnd = BubbleRadius,
            bottomEnd = BubbleRadius,
            bottomStart = if (groupEnd) BubbleTailRadius else senderCorner
        )
    }

    Column(
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (groupStart) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .then(if (hasImage) Modifier.width(imageWidth + 8.dp) else Modifier.widthIn(max = maxWidth))
                .clip(shape)
                .background(container)
                .clickable(enabled = failed, onClick = onRetry)
                .padding(if (hasImage) 4.dp else 0.dp)
        ) {
            when (attachment?.kind) {
                AttachmentKind.Image -> ImageAttachment(
                    attachment = attachment,
                    width = imageWidth,
                    onClick = onOpenAttachment
                )
                AttachmentKind.Pdf -> PdfAttachment(
                    attachment = attachment,
                    contentColor = content,
                    onClick = onOpenAttachment,
                    modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 6.dp)
                )
                null -> Unit
            }
            if (message.text.isNotBlank()) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = content,
                        modifier = Modifier.padding(
                            start = textStart,
                            end = textEnd,
                            top = if (attachment == null) 7.dp else 6.dp
                        )
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(start = textStart, end = textEnd, top = 2.dp, bottom = 5.dp)
            ) {
                Text(
                    text = formatMessageTime(message.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = content.copy(alpha = 0.7f)
                )
                if (mine) {
                    MessageStatusIcon(
                        status = message.status,
                        tint = content.copy(alpha = 0.85f),
                        size = 14.dp
                    )
                }
            }
        }
        if (failed) {
            Text(
                text = stringResource(R.string.text_10_9),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(onClick = onRetry)
            )
        }
    }
}

@Composable
private fun ImageAttachment(
    attachment: ChatAttachment,
    width: Dp,
    onClick: () -> Unit
) {
    val ratio = if (attachment.width > 0 && attachment.height > 0) {
        (attachment.width.toFloat() / attachment.height).coerceIn(IMAGE_RATIO_MIN, IMAGE_RATIO_MAX)
    } else {
        1f
    }
    val widthPx = with(LocalDensity.current) { width.roundToPx() }
    val image = rememberAttachmentImage(attachment.uri, widthPx)

    Box(
        modifier = Modifier
            .width(width)
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(BubbleRadius - 4.dp))
            .background(MaterialTheme.colorScheme.outline)
            .clickable(onClick = onClick)
    ) {
        Crossfade(targetState = image, label = "chatImage") { bitmap ->
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(R.string.text_10_16),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun PdfAttachment(
    attachment: ChatAttachment,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .widthIn(min = 200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = contentColor
            )
        }
        Column(modifier = Modifier.padding(start = 10.dp, end = 4.dp)) {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.text_10_24, fileSizeText(attachment.sizeBytes)),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorLine(@StringRes errorRes: Int?) {
    Text(
        text = errorRes?.let { stringResource(it, ATTACHMENT_MAX_MB) }.orEmpty(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun PendingAttachments(
    attachments: List<ChatAttachment>,
    importing: Boolean,
    onRemove: (ChatAttachment) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(attachments, key = { it.uri }) { attachment ->
            PendingTile(
                attachment = attachment,
                onRemove = { onRemove(attachment) },
                modifier = Modifier.animateItem()
            )
        }
        if (importing) {
            item(key = "importing") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(PendingTileSize)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun PendingTile(
    attachment: ChatAttachment,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizePx = with(LocalDensity.current) { PendingTileSize.roundToPx() }
    Box(
        modifier = modifier
            .size(PendingTileSize)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        when (attachment.kind) {
            AttachmentKind.Image -> {
                val image = rememberAttachmentImage(attachment.uri, sizePx)
                if (image != null) {
                    Image(
                        bitmap = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            AttachmentKind.Pdf -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.inverseSurface)
                .clickable(onClick = onRemove)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.text_10_22),
                tint = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun MessageInput(
    value: String,
    canSend: Boolean,
    canAttach: Boolean,
    onValueChange: (String) -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        IconButton(
            onClick = onAttach,
            enabled = canAttach,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = stringResource(R.string.text_10_15),
                tint = if (canAttach) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(R.string.text_10_10)) },
            shape = RoundedCornerShape(24.dp),
            maxLines = 5,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.weight(1f)
        )
        AnimatedVisibility(
            visible = canSend,
            enter = fadeIn() + scaleIn() + expandHorizontally(),
            exit = fadeOut() + scaleOut() + shrinkHorizontally()
        ) {
            FilledIconButton(
                onClick = onSend,
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 2.dp)
                    .size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.text_10_12)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachSheet(
    onPickPhotos: () -> Unit,
    onPickPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
                action()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.text_10_23),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
            AttachOption(
                icon = Icons.Default.Image,
                title = stringResource(R.string.text_10_16),
                hint = stringResource(R.string.text_10_18, ATTACHMENTS_PER_MESSAGE_MAX, ATTACHMENT_MAX_MB),
                onClick = { hideThen(onPickPhotos) }
            )
            AttachOption(
                icon = Icons.Default.PictureAsPdf,
                title = stringResource(R.string.text_10_17),
                hint = stringResource(R.string.text_10_19, ATTACHMENT_MAX_MB),
                onClick = { hideThen(onPickPdf) }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AttachOption(
    icon: ImageVector,
    title: String,
    hint: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildEntries(messages: List<ChatMessage>): List<ChatEntry> {
    val sorted = messages.sortedBy { it.sentAt }
    val result = ArrayList<ChatEntry>(sorted.size + 4)
    sorted.forEachIndexed { index, message ->
        val previous = sorted.getOrNull(index - 1)
        val next = sorted.getOrNull(index + 1)
        val day = message.sentAt.toLocalDate()
        if (previous == null || previous.sentAt.toLocalDate() != day) {
            result += DayEntry(day)
        }
        result += MessageEntry(
            message = message,
            groupStart = !sameGroup(previous, message),
            groupEnd = !sameGroup(message, next)
        )
    }
    return result.reversed()
}

private fun sameGroup(first: ChatMessage?, second: ChatMessage?): Boolean {
    if (first == null || second == null) return false
    if (first.event != null || second.event != null) return false
    return first.senderRole == second.senderRole &&
            first.sentAt.toLocalDate() == second.sentAt.toLocalDate() &&
            Duration.between(first.sentAt, second.sentAt).toMinutes() < GROUP_GAP_MINUTES
}
