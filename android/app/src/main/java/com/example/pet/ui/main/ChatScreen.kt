package com.example.pet.ui.main

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
import androidx.compose.ui.text.style.TextAlign
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
    val allChats by remember(role) { AppContainer.chats.chats(role) }.collectAsStateWithLifecycle()
    val chats = allChats.filter { it.lastMessage != null }

    NotificationPermissionPrompt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ScreenHeader(title = stringResource(R.string.text_10_1), onBack = onBack)

        Crossfade(
            targetState = chats.isEmpty(),
            label = "chatsEmpty",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { empty ->
            if (empty) {
                EmptyChats(role)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chats, key = { it.id }) { chat ->
                        ChatRow(
                            chat = chat,
                            role = role,
                            onClick = { onOpenChat(chat.id) },
                            modifier = Modifier.animateItem()
                        )
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val last = chat.lastMessage ?: return
    val mine = last.senderRole == role && last.event == null
    val hasUnread = chat.unreadCount > 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(12.dp)
    ) {
        InitialsAvatar(name = chat.companionName, photoUri = chat.companionAvatarUri, size = 52.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = shortPersonName(chat.companionName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
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

            Text(
                text = stringResource(R.string.text_10_5, chat.requestTitle, chat.requestDates),
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

@Composable
private fun EmptyChats(role: UserRole) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Chat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.text_10_2),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(if (role == UserRole.Owner) R.string.text_10_3 else R.string.text_10_4),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
