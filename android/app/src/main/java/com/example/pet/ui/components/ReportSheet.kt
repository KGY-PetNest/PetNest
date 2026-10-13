package com.example.pet.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.ReportReason
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSheet(
    reasons: List<ReportReason>,
    sentSubtitle: String,
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
            FormRules.descriptionError(comment, R.string.common_report_comment_error)
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

    AppBottomSheet(sheetState = sheetState, onDismissRequest = onDismiss) {
        AnimatedContent(
            targetState = sent,
            transitionSpec = { fadeThrough() },
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
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    SheetTitle(
                        text = stringResource(R.string.common_report_sent),
                        subtitle = sentSubtitle,
                        centered = true
                    )
                    Spacer(Modifier.height(FormButtonGap - 16.dp))
                    PrimaryButton(
                        text = stringResource(R.string.common_done),
                        onClick = { close() }
                    )
                    Spacer(Modifier.height(SheetBottomGap))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = ScreenTextPadding)
                ) {
                    SheetTitle(
                        text = stringResource(R.string.common_report),
                        subtitle = stringResource(R.string.common_report_what),
                        subtitleColor = if (reasonError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    reasons.forEach { option ->
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
                        placeholder = stringResource(R.string.common_report_comment_hint),
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

                    AnimatedVisibility(
                        visible = failed,
                        enter = revealEnter(),
                        exit = revealExit()
                    ) {
                        Text(
                            text = stringResource(R.string.common_send_failed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(FormButtonGap))
                    PrimaryButton(
                        text = stringResource(R.string.common_report_send),
                        loading = sending,
                        onClick = { submit() }
                    )
                    Spacer(Modifier.height(SheetBottomGap))
                }
            }
        }
    }
}
