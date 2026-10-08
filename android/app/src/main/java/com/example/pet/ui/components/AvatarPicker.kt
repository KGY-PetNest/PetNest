package com.example.pet.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pet.R

@Composable
fun AvatarPicker(
    name: String,
    photoUri: String?,
    onPhotoChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) pendingUri = uri
    }
    val openPicker = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    pendingUri?.let { uri ->
        AvatarCropDialog(
            uri = uri,
            onDismiss = { pendingUri = null },
            onCropped = { cropped ->
                onPhotoChange(cropped.toString())
                pendingUri = null
            }
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        val interaction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .pressScale(interaction, pressedScale = 0.94f)
                .clickable(interactionSource = interaction, indication = null) { openPicker() }
        ) {
            InitialsAvatar(name = name, photoUri = photoUri, size = 96.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = openPicker) {
                Text(stringResource(if (photoUri == null) R.string.text_17_11 else R.string.text_17_12))
            }
            if (photoUri != null) {
                TextButton(onClick = { onPhotoChange(null) }) {
                    Text(
                        text = stringResource(R.string.text_17_13),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}