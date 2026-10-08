package com.example.pet.ui.chat

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.AttachmentKind
import com.example.pet.data.ChatMessage
import com.example.pet.data.MockData
import com.example.pet.data.MyResponseStatus
import com.example.pet.data.RequestStatus
import com.example.pet.data.UserRole
import com.example.pet.data.phoneForDial
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.MyResponseStatusChip
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.SettingsRow
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.formatPhone
import com.example.pet.ui.main.StatusChip

private const val MEDIA_COLUMNS = 3

@Composable
fun ChatInfoScreen(
    chatId: String,
    role: UserRole,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenRequest: (String) -> Unit,
    onOpenAttachment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chats by remember(role) { AppContainer.chats.chats(role) }.collectAsStateWithLifecycle()
    val messages by remember(chatId) { AppContainer.chats.messages(chatId) }.collectAsStateWithLifecycle()
    val ownerRequests by AppContainer.requests.ownerRequests.collectAsStateWithLifecycle()
    val feed by AppContainer.requests.feed.collectAsStateWithLifecycle()
    val responded by AppContainer.requests.respondedIds.collectAsStateWithLifecycle()
    val volunteers by AppContainer.volunteers.volunteers.collectAsStateWithLifecycle()

    val chat = chats.firstOrNull { it.id == chatId }
    if (chat == null) {
        NotFoundScreen(title = stringResource(R.string.text_23_1), onBack = onBack, modifier = modifier)
        return
    }
    val request = when (role) {
        UserRole.Owner -> ownerRequests
        UserRole.Volunteer -> feed
    }.firstOrNull { it.id == chat.requestId }
    val myStatus = if (role == UserRole.Volunteer) {
        request?.statusFor(MockData.CURRENT_VOLUNTEER_ID, responded)
    } else {
        null
    }
    val companionPhone = when (role) {
        UserRole.Volunteer -> request?.ownerPhone?.takeIf {
            myStatus == MyResponseStatus.Chosen || myStatus == MyResponseStatus.Completed
        }
        UserRole.Owner -> volunteers.firstOrNull { it.id == chat.volunteerId }?.phone?.takeIf {
            request != null && request.chosenVolunteerId == chat.volunteerId && request.status != RequestStatus.Open
        }
    }?.takeIf { it.isNotBlank() }
    val media = remember(messages) {
        messages.filter { it.attachment != null }.sortedByDescending { it.sentAt }
    }
    val safety = rememberChatSafetyState()
    val images = media.filter { it.attachment?.kind == AttachmentKind.Image }
    val documents = media.filter { it.attachment?.kind == AttachmentKind.Pdf }

    ChatSafetyDialogs(state = safety, chat = chat)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(title = stringResource(R.string.text_23_1), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InitialsAvatar(name = chat.companionName, photoUri = chat.companionAvatarUri, size = 96.dp, zoomable = true)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = chat.companionName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(if (role == UserRole.Owner) R.string.text_23_2 else R.string.text_23_3),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (companionPhone != null) {
                        Spacer(Modifier.height(6.dp))
                        IconLine(
                            icon = Icons.Default.Phone,
                            text = formatPhone(companionPhone),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                val actions = buildList {
                    if (role == UserRole.Owner) {
                        add(
                            InfoAction(Icons.Default.Person, R.string.text_23_4) { onOpenProfile(chat.volunteerId) }
                        )
                    }
                    if (companionPhone != null) {
                        add(
                            InfoAction(Icons.Default.Phone, R.string.text_23_5) {
                                val intent = Intent(Intent.ACTION_DIAL, "tel:${phoneForDial(companionPhone)}".toUri())
                                runCatching { context.startActivity(intent) }
                            }
                        )
                    }
                }
                if (actions.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        actions.forEach { action -> ActionTile(action) }
                    }
                }

                SectionDivider()
                SectionTitle(stringResource(R.string.text_23_6))
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardSurface { onOpenRequest(chat.requestId) }
                        .padding(12.dp)
                ) {
                    PetThumbnail(photoUri = request?.petPhotoUri ?: chat.petPhotoUri, size = 52.dp)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = chat.requestTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            when {
                                myStatus != null -> MyResponseStatusChip(myStatus)
                                role == UserRole.Owner && request != null -> StatusChip(request.status)
                            }
                        }
                        IconLine(icon = Icons.Default.DateRange, text = chat.requestDates, maxLines = 1)
                    }
                }

                SectionDivider()
                SectionTitle(stringResource(R.string.text_23_7))
                Spacer(Modifier.height(12.dp))
                if (media.isEmpty()) {
                    Text(
                        text = stringResource(R.string.text_23_8),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    MediaGrid(images = images, onOpen = onOpenAttachment)
                    if (images.isNotEmpty() && documents.isNotEmpty()) Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        documents.forEach { message ->
                            val attachment = message.attachment ?: return@forEach
                            PdfAttachment(
                                attachment = attachment,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                onClick = { onOpenAttachment(message.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cardSurface()
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                SectionDivider()
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsRow(
                        icon = Icons.Default.Flag,
                        text = stringResource(R.string.text_10_32),
                        onClick = { safety.reportOpen = true },
                        danger = true
                    )
                    SettingsRow(
                        icon = Icons.Default.Block,
                        text = stringResource(if (chat.blocked) R.string.text_10_34 else R.string.text_10_33),
                        onClick = { safety.blockDialogOpen = true },
                        danger = !chat.blocked
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private class InfoAction(
    val icon: ImageVector,
    val label: Int,
    val onClick: () -> Unit
)

@Composable
private fun ActionTile(action: InfoAction) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(104.dp)
            .cardSurface(action.onClick)
            .padding(vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(imageVector = action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(action.label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MediaGrid(images: List<ChatMessage>, onOpen: (String) -> Unit) {
    if (images.isEmpty()) return
    val tilePx = with(LocalDensity.current) { 160.dp.roundToPx() }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        images.chunked(MEDIA_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { message ->
                    val attachment = message.attachment ?: return@forEach
                    val image = rememberAttachmentImage(attachment.uri, tilePx)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.outline)
                            .clickable { onOpen(message.id) }
                    ) {
                        if (image != null) {
                            Image(
                                bitmap = image,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                repeat(MEDIA_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(20.dp))
}
