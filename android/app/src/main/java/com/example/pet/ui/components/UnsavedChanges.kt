package com.example.pet.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.pet.R

@Composable
fun rememberLeaveGuard(hasChanges: Boolean, onLeave: () -> Unit): () -> Unit {
    var asking by rememberSaveable { mutableStateOf(false) }
    val currentHasChanges by rememberUpdatedState(hasChanges)
    val currentOnLeave by rememberUpdatedState(onLeave)

    BackHandler(enabled = hasChanges) { asking = true }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text(stringResource(R.string.common_discard_title)) },
            text = { Text(stringResource(R.string.common_discard_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        asking = false
                        currentOnLeave()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.common_discard_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { asking = false }) {
                    Text(stringResource(R.string.common_discard_keep))
                }
            }
        )
    }

    return { if (currentHasChanges) asking = true else currentOnLeave() }
}
