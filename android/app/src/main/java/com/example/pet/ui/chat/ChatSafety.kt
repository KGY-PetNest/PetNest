package com.example.pet.ui.chat

import com.example.pet.ui.components.ScreenTextPadding
import com.example.pet.ui.components.revealEnter
import com.example.pet.ui.components.revealExit
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.Chat
import com.example.pet.data.ReportReason
import com.example.pet.data.shortPersonName
import com.example.pet.notifications.ChatNotifier
import kotlinx.coroutines.launch
import com.example.pet.ui.components.ReportSheet

private val ChatReportReasons = ReportReason.entries - ReportReason.Fake

@Stable
class ChatSafetyState {
    var reportOpen by mutableStateOf(false)
    var blockDialogOpen by mutableStateOf(false)
    var deleteDialogOpen by mutableStateOf(false)
    var deletedChat by mutableStateOf<Chat?>(null)
}

@Composable
fun rememberChatSafetyState(): ChatSafetyState = remember { ChatSafetyState() }

@Composable
fun ChatSafetyDialogs(state: ChatSafetyState, chat: Chat, onDeleted: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val name = shortPersonName(chat.companionName)

    if (state.deleteDialogOpen) {
        DeleteChatsDialog(
            count = 1,
            onDelete = {
                state.deletedChat = chat
                val deleted = AppContainer.chats.delete(chat.id).isSuccess
                if (deleted) {
                    ChatNotifier.cancel(context, chat.id)
                    state.deleteDialogOpen = false
                    onDeleted()
                } else {
                    state.deletedChat = null
                }
                deleted
            },
            onDismiss = { state.deleteDialogOpen = false }
        )
    }

    if (state.blockDialogOpen) {
        val unblock = chat.blocked
        AlertDialog(
            onDismissRequest = { state.blockDialogOpen = false },
            title = { Text(stringResource(if (unblock) R.string.text_10_37 else R.string.text_10_35)) },
            text = { Text(stringResource(if (unblock) R.string.text_10_38 else R.string.text_10_36, name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.blockDialogOpen = false
                        scope.launch {
                            if (unblock) AppContainer.chats.unblock(chat.id) else AppContainer.chats.block(chat.id)
                        }
                    }
                ) {
                    Text(
                        text = stringResource(if (unblock) R.string.text_10_34 else R.string.text_10_33),
                        color = if (unblock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { state.blockDialogOpen = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (state.reportOpen) {
        ReportSheet(
            reasons = ChatReportReasons,
            sentSubtitle = stringResource(R.string.text_10_50),
            canBlock = !chat.blocked,
            onSubmit = { reason, comment, alsoBlock ->
                val result = AppContainer.chats.report(chat.id, reason, comment)
                if (result.isSuccess && alsoBlock) AppContainer.chats.block(chat.id)
                result
            },
            onDismiss = { state.reportOpen = false }
        )
    }
}

@Composable
fun DeleteChatsDialog(
    count: Int,
    onDelete: suspend () -> Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var deleting by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val single = count <= 1

    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        title = { Text(stringResource(if (single) R.string.text_10_59 else R.string.text_10_62)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(if (single) R.string.text_10_60 else R.string.text_10_63))
                AnimatedVisibility(visible = failed, enter = revealEnter(), exit = revealExit()) {
                    Text(
                        text = stringResource(if (single) R.string.text_10_61 else R.string.text_10_69),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !deleting,
                onClick = {
                    deleting = true
                    failed = false
                    scope.launch {
                        val deleted = onDelete()
                        deleting = false
                        failed = !deleted
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.common_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(enabled = !deleting, onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun BlockedBanner(onUnblock: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenTextPadding, vertical = 10.dp)
    ) {
        Text(
            text = stringResource(R.string.text_10_39),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onUnblock) {
            Text(stringResource(R.string.text_10_34))
        }
    }
}
