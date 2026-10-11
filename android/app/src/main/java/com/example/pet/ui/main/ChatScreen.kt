package com.example.pet.ui.main

import com.example.pet.ui.components.ScreenHorizontalPadding
import com.example.pet.ui.components.PersonThumbnailSize
import com.example.pet.ui.components.EmptyState
import com.example.pet.ui.components.ListItemSpacing
import com.example.pet.ui.components.ScreenTopGap
import com.example.pet.ui.components.ScreenBottomGap
import com.example.pet.ui.components.ScreenContentInset
import com.example.pet.ui.components.CardPadding
import com.example.pet.ui.components.FilterPill
import com.example.pet.ui.theme.extraColors
import com.example.pet.ui.components.ShortAnimMs
import com.example.pet.ui.components.fadeThrough
import com.example.pet.ui.chat.DeleteChatsDialog
import com.example.pet.notifications.ChatNotifier
import com.example.pet.data.MyResponseStatus
import com.example.pet.data.RequestStatus
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.AttachmentKind
import com.example.pet.data.Chat
import com.example.pet.data.UserRole
import com.example.pet.data.shortPersonName
import com.example.pet.ui.chat.MessageStatusIcon
import com.example.pet.ui.chat.chatEventText
import com.example.pet.ui.chat.formatChatListTime
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.NotificationPermissionPrompt
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.cardSurface

private const val UNREAD_BADGE_MAX = 99

@Composable
fun ChatScreen(
    role: UserRole,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allChats by remember(role) { AppContainer.chats.chats(role) }.collectAsStateWithLifecycle()
    val ownerRequests by AppContainer.requests.ownerRequests.collectAsStateWithLifecycle()
    val feed by AppContainer.requests.feed.collectAsStateWithLifecycle()
    val responded by AppContainer.requests.respondedIds.collectAsStateWithLifecycle()
    var unreadOnly by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    var deleteDialogOpen by rememberSaveable { mutableStateOf(false) }

    val activeChatIds = remember(allChats, ownerRequests, feed, responded, role) {
        allChats.filter { chat ->
            when (role) {
                UserRole.Owner -> ownerRequests.firstOrNull { it.id == chat.requestId }?.let {
                    it.status == RequestStatus.VolunteerChosen && it.chosenVolunteerId == chat.volunteerId
                } ?: false
                UserRole.Volunteer -> feed.firstOrNull { it.id == chat.requestId }
                    ?.statusFor(chat.volunteerId, responded) == MyResponseStatus.Chosen
            }
        }.map { it.id }.toSet()
    }
    val chats = remember(allChats, activeChatIds) {
        allChats.filter { it.lastMessage != null }.sortedBy { it.id !in activeChatIds }
    }
    val visibleChats = if (unreadOnly) chats.filter { it.unreadCount > 0 } else chats
    val selection = selectedIds.filter { id -> chats.any { it.id == id } }
    val selecting = selection.isNotEmpty()
    val selectionHasUnread = chats.any { it.id in selection && it.unreadCount > 0 }

    fun toggle(chatId: String) {
        selectedIds = if (chatId in selection) selection - chatId else selection + chatId
    }

    BackHandler(enabled = selecting) { selectedIds = emptyList() }

    NotificationPermissionPrompt()

    if (deleteDialogOpen && selecting) {
        DeleteChatsDialog(
            count = selection.size,
            onDelete = {
                val ids = selection
                val failed = coroutineScope {
                    ids.map { id -> async { id to AppContainer.chats.delete(id).isSuccess } }.awaitAll()
                }.mapNotNull { (id, deleted) ->
                    if (deleted) {
                        ChatNotifier.cancel(context, id)
                        null
                    } else {
                        id
                    }
                }
                selectedIds = failed
                if (failed.isEmpty()) deleteDialogOpen = false
                failed.isEmpty()
            },
            onDismiss = { deleteDialogOpen = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenHorizontalPadding)
    ) {
        AnimatedContent(
            targetState = selecting,
            transitionSpec = { fadeThrough() },
            label = "chatHeader"
        ) { selectionMode ->
            if (selectionMode) {
                ScreenHeader(
                    title = stringResource(R.string.text_10_64, selection.size),
                    onBack = { selectedIds = emptyList() },
                    actions = {
                        IconButton(
                            enabled = selectionHasUnread,
                            onClick = {
                                val ids = selection
                                selectedIds = emptyList()
                                scope.launch {
                                    ids.forEach { id ->
                                        AppContainer.chats.markRead(id)
                                        ChatNotifier.cancel(context, id)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = stringResource(R.string.text_10_65),
                                tint = if (selectionHasUnread) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        IconButton(onClick = { deleteDialogOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.text_10_58),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            } else {
                ScreenHeader(title = stringResource(R.string.text_10_1), onBack = onBack)
            }
        }

        Crossfade(
            targetState = chats.isEmpty(),
            label = "chatsEmpty",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { empty ->
            if (empty) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.Chat,
                    title = stringResource(R.string.text_10_2),
                    text = stringResource(if (role == UserRole.Owner) R.string.text_10_3 else R.string.text_10_4),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = ScreenTopGap)
                    ) {
                        FilterPill(
                            text = stringResource(R.string.common_all),
                            selected = !unreadOnly,
                            onClick = { unreadOnly = false }
                        )
                        FilterPill(
                            text = stringResource(R.string.text_10_66),
                            selected = unreadOnly,
                            onClick = { unreadOnly = true }
                        )
                    }

                    Crossfade(
                        targetState = visibleChats.isEmpty(),
                        label = "chatsFiltered",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { filteredEmpty ->
                        if (filteredEmpty) {
                            Text(
                                text = stringResource(R.string.text_10_67),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = ScreenContentInset, vertical = ListItemSpacing)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
                                contentPadding = PaddingValues(top = ListItemSpacing, bottom = ScreenBottomGap),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(visibleChats, key = { it.id }) { chat ->
                                    ChatRow(
                                        chat = chat,
                                        role = role,
                                        active = chat.id in activeChatIds,
                                        selected = chat.id in selection,
                                        onClick = { if (selecting) toggle(chat.id) else onOpenChat(chat.id) },
                                        onLongClick = { toggle(chat.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: Chat,
    role: UserRole,
    active: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val last = chat.lastMessage ?: return
    val mine = last.senderRole == role && last.event == null
    val hasUnread = chat.unreadCount > 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(onClick = onClick, onLongClick = onLongClick, selected = selected)
            .padding(CardPadding)
    ) {
        Box {
            InitialsAvatar(name = chat.companionName, photoUri = chat.companionAvatarUri, size = PersonThumbnailSize)
            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(ShortAnimMs)) + scaleIn(tween(ShortAnimMs)),
                exit = fadeOut(tween(ShortAnimMs)) + scaleOut(tween(ShortAnimMs)),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = shortPersonName(chat.companionName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (chat.blocked) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = stringResource(R.string.text_10_31),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (mine) {
                    MessageStatusIcon(
                        status = last.status,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = formatChatListTime(last.sentAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val requestLine = stringResource(R.string.text_10_5, chat.requestTitle, chat.requestDates)
            val activeLabel = stringResource(R.string.text_10_68)
            val activeColor = if (role == UserRole.Owner) MaterialTheme.extraColors.warning else MaterialTheme.extraColors.success
            Text(
                text = if (active) {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = activeColor, fontWeight = FontWeight.Bold)) { append(activeLabel) }
                        append(stringResource(R.string.text_10_70))
                        append(requestLine)
                    }
                } else {
                    AnnotatedString(requestLine)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                val attachment = last.attachment
                if (attachment != null) {
                    Icon(
                        imageVector = when (attachment.kind) {
                            AttachmentKind.Image -> Icons.Default.Image
                            AttachmentKind.Pdf -> Icons.Default.PictureAsPdf
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(16.dp)
                    )
                }
                val event = last.event
                val preview = if (event != null) {
                    chatEventText(event, role)
                } else {
                    last.text.ifBlank {
                        when (attachment?.kind) {
                            AttachmentKind.Image -> stringResource(R.string.text_10_16)
                            AttachmentKind.Pdf -> attachment?.name.orEmpty()
                            null -> ""
                        }
                    }
                }
                Text(
                    text = if (mine) stringResource(R.string.text_10_6, preview) else preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                AnimatedVisibility(
                    visible = hasUnread,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    UnreadBadge(count = chat.unreadCount, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(22.dp)
            .widthIn(min = 22.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = if (count > UNREAD_BADGE_MAX) "$UNREAD_BADGE_MAX+" else count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
