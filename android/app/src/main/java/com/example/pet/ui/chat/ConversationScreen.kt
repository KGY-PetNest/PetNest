package com.example.pet.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.Chat
import com.example.pet.data.ChatMessage
import com.example.pet.data.MessageStatus
import com.example.pet.data.UserRole
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

private const val GROUP_GAP_MINUTES = 5L
private const val BUBBLE_WIDTH_FRACTION = 0.78f
private val BubbleMaxWidth = 480.dp
private val BubbleRadius = 18.dp
private val BubbleTailRadius = 4.dp

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
    onOpenRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val chats by remember(role) { AppContainer.chats.chats(role) }.collectAsStateWithLifecycle()
    val messages by remember(chatId) { AppContainer.chats.messages(chatId) }.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var draft by rememberSaveable { mutableStateOf("") }
    val entries = remember(messages) { buildEntries(messages) }
    val lastMessage = messages.lastOrNull()

    LaunchedEffect(lastMessage?.id) {
        if (lastMessage != null && (lastMessage.senderRole == role || listState.firstVisibleItemIndex <= 2)) {
            listState.animateScrollToItem(0)
        }
        AppContainer.chats.markRead(chatId)
    }

    val chat = chats.firstOrNull { it.id == chatId }
    if (chat == null) {
        NotFoundScreen(title = stringResource(R.string.text_10_1), onBack = onBack, modifier = modifier)
        return
    }

    fun send() {
        val text = draft.trim()
        if (text.isEmpty()) return
        draft = ""
        scope.launch { AppContainer.chats.send(chatId, role, text) }
    }

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
                onOpenRequest = { onOpenRequest(chat.requestId) }
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
                            is MessageEntry -> MessageBubble(
                                message = entry.message,
                                mine = entry.message.senderRole == role,
                                groupStart = entry.groupStart,
                                groupEnd = entry.groupEnd,
                                onRetry = {
                                    scope.launch { AppContainer.chats.resend(chatId, entry.message.id) }
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            MessageInput(
                value = draft,
                onValueChange = { draft = it.take(FormRules.LONG_TEXT_MAX_LENGTH) },
                onSend = { send() }
            )
        }
    }
}

@Composable
private fun ConversationHeader(
    chat: Chat,
    onBack: () -> Unit,
    onOpenRequest: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 4.dp, end = 16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenRequest)
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
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxWidth = (screenWidth * BUBBLE_WIDTH_FRACTION).coerceAtMost(BubbleMaxWidth)
    val failed = message.status == MessageStatus.Failed
    val container = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val content = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
                .widthIn(max = maxWidth)
                .clip(shape)
                .background(container)
                .clickable(enabled = failed, onClick = onRetry)
                .padding(start = 12.dp, end = 10.dp, top = 7.dp, bottom = 5.dp)
        ) {
            SelectionContainer {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = content
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp)
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
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
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
            visible = value.isNotBlank(),
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
    return first.senderRole == second.senderRole &&
            first.sentAt.toLocalDate() == second.sentAt.toLocalDate() &&
            Duration.between(first.sentAt, second.sentAt).toMinutes() < GROUP_GAP_MINUTES
}
