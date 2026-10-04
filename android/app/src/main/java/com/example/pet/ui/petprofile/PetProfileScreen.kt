package com.example.pet.ui.petprofile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.AvatarCropDialog
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.adaptiveContentWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PetProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var animal by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var features by rememberSaveable { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) pendingUri = uri }

    val avatar by produceState<ImageBitmap?>(initialValue = null, photoUri) {
        value = photoUri?.let { uri ->
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    pendingUri?.let { uri ->
        AvatarCropDialog(
            uri = uri,
            onDismiss = { pendingUri = null },
            onCropped = { cropped ->
                photoUri = cropped
                pendingUri = null
            }
        )
    }

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
            ScreenHeader(title = stringResource(R.string.text_4_1), onBack = onBack)

            val scrollState = rememberScrollState()
            val density = LocalDensity.current
            var topContentHeightDp by remember { mutableStateOf(0.dp) }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val dynamicFeaturesHeight = if (topContentHeightDp > 0.dp) {
                    (maxHeight - topContentHeightDp - 24.dp).coerceAtLeast(120.dp)
                } else {
                    120.dp
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            state = scrollState,
                            enabled = scrollState.maxValue > 0
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                topContentHeightDp = with(density) { coordinates.size.height.toDp() }
                            }
                    ) {
                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(112.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val bmp = avatar
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.text_4_2),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        )

                        Spacer(Modifier.height(20.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LabeledField(
                                label = stringResource(R.string.text_4_3),
                                value = name,
                                onValueChange = { name = it }
                            )
                            LabeledField(
                                label = stringResource(R.string.text_4_4),
                                value = animal,
                                onValueChange = { animal = it }
                            )
                            LabeledField(
                                label = stringResource(R.string.text_4_5),
                                value = age,
                                onValueChange = { age = it.filter(Char::isDigit) },
                                keyboardType = KeyboardType.Number
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.text_4_6),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = features,
                        onValueChange = { features = it },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .height(dynamicFeaturesHeight)
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }

            PrimaryButton(
                text = stringResource(R.string.text_4_7),
                onClick = onSave
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}