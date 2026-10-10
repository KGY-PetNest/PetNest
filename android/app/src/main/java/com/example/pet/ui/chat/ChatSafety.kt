package com.example.pet.ui.chat

import com.example.pet.ui.components.ScreenTextPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.Chat
import com.example.pet.data.ReportReason
import com.example.pet.data.shortPersonName
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.label
import kotlinx.coroutines.launch

@Stable
class ChatSafetyState {
    var reportOpen by mutableStateOf(false)
    var blockDialogOpen by mutableStateOf(false)
}

@Composable
fun rememberChatSafetyState(): ChatSafetyState = remember { ChatSafetyState() }

@Composable
fun ChatSafetyDialogs(state: ChatSafetyState, chat: Chat) {
    val scope = rememberCoroutineScope()
    val name = shortPersonName(chat.companionName)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSheet(
    canBlock: Boolean,
    onSubmit: suspend (ReportReason, String, Boolean) -> Result<Unit>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var reason by rememberSaveable { mutableStateOf<ReportReason?>(null) }
    var comment by rememberSaveable { mutableStateOf("") }
    var alsoBlock by rememberSaveable { mutableStateOf(canBlock) }
    var sending by remember { mutableStateOf(false) }
    var sent by rememberSaveable { mutableStateOf(false) }
    var reasonError by rememberSaveable { mutableStateOf(false) }
    var commentError by rememberSaveable { mutableStateOf<Int?>(null) }
    var failed by rememberSaveable { mutableStateOf(false) }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    fun submit() {
        val selected = reason
        reasonError = selected == null
        commentError = if (selected == ReportReason.Other) {
            FormRules.descriptionError(comment, R.string.text_10_51)
        } else {
            null
        }
        if (selected == null || commentError != null) return
        scope.launch {
            sending = true
            failed = false
            val result = onSubmit(selected, comment.trim(), canBlock && alsoBlock)
            sending = false
            if (result.isSuccess) sent = true else failed = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        AnimatedContent(
            targetState = sent,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "reportSent"
        ) { done ->
            if (done) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenTextPadding)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.text_10_49),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.text_10_50),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton(
                        text = stringResource(R.string.common_done),
                        onClick = { close() },
                        height = 48.dp
                    )
                    Spacer(Modifier.height(24.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = ScreenTextPadding)
                ) {
                    Text(
                        text = stringResource(R.string.text_10_32),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.text_10_40),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (reasonError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    ReportReason.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    reason = option
                                    reasonError = false
                                    commentError = null
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = reason == option,
                                onClick = {
                                    reason = option
                                    reasonError = false
                                    commentError = null
                                }
                            )
                            Text(
                                text = stringResource(option.label),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = comment,
                        onValueChange = {
                            comment = it
                            commentError = null
                        },
                        placeholder = stringResource(R.string.text_10_46),
                        errorText = commentError?.let { stringResource(it, FormRules.DESCRIPTION_MIN_LENGTH) },
                        singleLine = false,
                        imeAction = ImeAction.Default,
                        modifier = Modifier.height(112.dp)
                    )

                    if (canBlock) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { alsoBlock = !alsoBlock }
                        ) {
                            Checkbox(checked = alsoBlock, onCheckedChange = { alsoBlock = it })
                            Text(
                                text = stringResource(R.string.text_10_47),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    AnimatedVisibility(visible = failed) {
                        Text(
                            text = stringResource(R.string.text_10_52),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = stringResource(R.string.text_10_48),
                        loading = sending,
                        onClick = { submit() },
                        height = 48.dp
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun BlockedBanner(onUnblock: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
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
