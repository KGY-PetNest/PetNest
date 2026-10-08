package com.example.pet.ui.petprofile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.pet.R
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.pet.data.Pet
import com.example.pet.data.AppContainer
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Delete
import com.example.pet.data.PetTrait
import com.example.pet.ui.components.AvatarCropDialog
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PetTraitSelector
import com.example.pet.ui.components.PinnedBottomBarLayout
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import com.example.pet.ui.components.pressScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PetProfileScreen(
    petId: String?,
    onBack: () -> Unit,
    onSave: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val existing = remember(petId) {
        petId?.let { id -> AppContainer.pets.pets.value.firstOrNull { it.id == id } }
    }

    var photoUri by rememberSaveable { mutableStateOf(existing?.photoUri?.let(Uri::parse)) }
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var animal by rememberSaveable { mutableStateOf(existing?.animal.orEmpty()) }
    var age by rememberSaveable { mutableStateOf(existing?.age?.toString().orEmpty()) }
    var features by rememberSaveable { mutableStateOf(existing?.features.orEmpty()) }
    var traits by rememberSaveable { mutableStateOf(existing?.traits?.toSet() ?: emptySet()) }
    var saving by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    var nameError by rememberSaveable { mutableStateOf(false) }
    var animalError by rememberSaveable { mutableStateOf(false) }
    var ageError by rememberSaveable { mutableStateOf(false) }
    var featuresError by rememberSaveable { mutableStateOf<Int?>(null) }

    fun submit() {
        nameError = name.isBlank()
        animalError = animal.isBlank()
        ageError = age.isBlank()
        featuresError = FormRules.descriptionError(features, R.string.text_4_13)
        if (!nameError && !animalError && !ageError && featuresError == null) {
            focusManager.clearFocus()
            val pet = Pet(
                id = existing?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                animal = animal.trim(),
                age = age.toIntOrNull() ?: 0,
                traits = PetTrait.entries.filter { it in traits },
                features = features.trim(),
                photoUri = photoUri?.toString()
            )
            scope.launch {
                saving = true
                AppContainer.pets.save(pet)
                saving = false
                onSave(pet.id)
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) pendingUri = uri }

    val openPicker = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

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

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.text_4_16)) },
            text = { Text(stringResource(R.string.text_4_17, existing.name)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        AppContainer.pets.delete(existing.id)
                        onSave(null)
                    }
                }) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    val nextField = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    Box(
        modifier = modifier
            .fillMaxSize()
            .clearFocusOnTap(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(
                title = stringResource(if (existing != null) R.string.text_4_15 else R.string.text_4_1),
                onBack = onBack,
                actions = if (existing != null) {
                    {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.text_4_16),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    null
                }
            )

            PinnedBottomBarLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                bottomBar = {
                    PrimaryButton(
                        text = stringResource(R.string.text_4_7),
                        loading = saving,
                        onClick = { submit() }
                    )
                    Spacer(Modifier.height(32.dp))
                }
            ) { imeOverlap ->
                val scrollState = rememberScrollState()
                val density = LocalDensity.current
                var topContentHeightDp by remember { mutableStateOf(0.dp) }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val stableHeight = maxHeight + imeOverlap
                    val dynamicFeaturesHeight = if (topContentHeightDp > 0.dp) {
                        (stableHeight - topContentHeightDp - 16.dp).coerceAtLeast(FEATURES_MIN_HEIGHT)
                    } else {
                        FEATURES_MIN_HEIGHT
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    topContentHeightDp = with(density) { coordinates.size.height.toDp() }
                                }
                        ) {
                            Spacer(Modifier.height(8.dp))

                            val avatarInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(100.dp)
                                    .pressScale(avatarInteraction, pressedScale = 0.94f)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable(
                                        interactionSource = avatarInteraction,
                                        indication = null
                                    ) { openPicker() },
                                contentAlignment = Alignment.Center
                            ) {
                                Crossfade(targetState = avatar, label = "avatar") { bmp ->
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Pets,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = stringResource(R.string.text_4_2),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { openPicker() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Text(
                                text = stringResource(R.string.text_4_25),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 2.dp)
                            )

                            Spacer(Modifier.height(20.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LabeledField(
                                    label = stringResource(R.string.text_4_3),
                                    required = true,
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        nameError = false
                                    },
                                    errorText = if (nameError) stringResource(R.string.text_4_10) else null,
                                    placeholder = stringResource(R.string.text_4_18),
                                    keyboardActions = nextField
                                )
                                LabeledField(
                                    label = stringResource(R.string.text_4_4),
                                    required = true,
                                    value = animal,
                                    onValueChange = {
                                        animal = it
                                        animalError = false
                                    },
                                    errorText = if (animalError) stringResource(R.string.text_4_11) else null,
                                    placeholder = stringResource(R.string.text_4_19),
                                    keyboardActions = nextField
                                )
                                LabeledField(
                                    label = stringResource(R.string.text_4_5),
                                    required = true,
                                    value = age,
                                    onValueChange = {
                                        age = it.filter(Char::isDigit).take(FormRules.PET_AGE_MAX_DIGITS)
                                        ageError = false
                                    },
                                    errorText = if (ageError) stringResource(R.string.text_4_12) else null,
                                    placeholder = stringResource(R.string.text_4_21),
                                    keyboardType = KeyboardType.Number,
                                    keyboardActions = nextField
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            FieldLabel(
                                text = stringResource(R.string.text_4_6),
                                required = true,
                                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                            )
                            Text(
                                text = stringResource(R.string.text_4_23),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
                            )

                            PetTraitSelector(
                                selected = traits,
                                onToggle = { trait ->
                                    traits = if (trait in traits) traits - trait else traits + trait
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(Modifier.height(12.dp))
                        }

                        val featuresErrorText = featuresError?.let {
                            stringResource(it, FormRules.DESCRIPTION_MIN_LENGTH)
                        }
                        OutlinedTextField(
                            value = features,
                            onValueChange = {
                                features = it.take(FormRules.LONG_TEXT_MAX_LENGTH)
                                featuresError = null
                            },
                            placeholder = { Text(stringResource(R.string.text_4_14)) },
                            isError = featuresError != null,
                            supportingText = featuresErrorText?.let { { Text(it) } },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(dynamicFeaturesHeight)
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private val FEATURES_MIN_HEIGHT = 136.dp

@Composable
private fun FieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    val errorColor = MaterialTheme.colorScheme.error
    Text(
        text = buildAnnotatedString {
            append(text)
            if (required) {
                withStyle(SpanStyle(color = errorColor)) { append(" *") }
            }
        },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    placeholder: String? = null,
    maxLength: Int = FormRules.SHORT_TEXT_MAX_LENGTH
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        FieldLabel(
            text = label,
            required = required,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.take(maxLength)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            isError = errorText != null,
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else {
                null
            },
            supportingText = errorText?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth()
        )
    }
}