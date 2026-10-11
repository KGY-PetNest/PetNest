package com.example.pet.ui.petprofile

import com.example.pet.ui.components.ScreenContentInset
import com.example.pet.ui.components.ScreenHorizontalPadding
import com.example.pet.ui.components.AvatarPicker
import com.example.pet.ui.components.FieldError
import com.example.pet.ui.components.FieldHint
import com.example.pet.ui.components.FieldLabel
import com.example.pet.ui.components.ScreenTopGap
import com.example.pet.ui.components.ScrollEndGap
import com.example.pet.ui.components.FormFieldSpacing
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.Pet
import com.example.pet.data.PetKind
import com.example.pet.data.recognizedKind
import com.example.pet.data.repository.PetInUseException
import com.example.pet.data.PetTrait
import com.example.pet.data.RequestStatus
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PetTraitSelector
import com.example.pet.ui.components.PinnedBottomBarLayout
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SelectableChips
import com.example.pet.ui.components.label
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import java.util.UUID
import kotlinx.coroutines.launch
import com.example.pet.ui.components.rememberLeaveGuard

@Composable
fun PetProfileScreen(
    petId: String?,
    onBack: () -> Unit,
    onSave: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val existing = remember(petId) {
        petId?.let { id -> AppContainer.pets.pets.value.firstOrNull { it.id == id } }
    }

    var photoUri by rememberSaveable { mutableStateOf(existing?.photoUri) }
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var animal by rememberSaveable { mutableStateOf(existing?.animal.orEmpty()) }
    var kind by rememberSaveable { mutableStateOf(existing?.kind) }
    var kindManual by rememberSaveable {
        mutableStateOf(existing != null && existing.kind != recognizedKind(existing.animal))
    }
    var age by rememberSaveable { mutableStateOf(existing?.age?.toString().orEmpty()) }
    var features by rememberSaveable { mutableStateOf(existing?.features.orEmpty()) }
    var traits by rememberSaveable { mutableStateOf(existing?.traits?.toSet() ?: emptySet()) }
    var saving by remember { mutableStateOf(false) }

    val hasChanges = photoUri != existing?.photoUri ||
            name != existing?.name.orEmpty() ||
            animal != existing?.animal.orEmpty() ||
            kind != existing?.kind ||
            age != existing?.age?.toString().orEmpty() ||
            features != existing?.features.orEmpty() ||
            traits != (existing?.traits?.toSet() ?: emptySet<PetTrait>())
    val leave = rememberLeaveGuard(hasChanges = hasChanges && !saving, onLeave = onBack)
    var confirmDelete by remember { mutableStateOf(false) }

    var nameError by rememberSaveable { mutableStateOf(false) }
    var animalError by rememberSaveable { mutableStateOf(false) }
    var kindError by rememberSaveable { mutableStateOf(false) }
    var ageError by rememberSaveable { mutableStateOf(false) }
    var featuresError by rememberSaveable { mutableStateOf<Int?>(null) }

    fun submit() {
        if (saving) return
        nameError = name.isBlank()
        animalError = animal.isBlank()
        kindError = kind == null
        ageError = age.isBlank()
        featuresError = FormRules.descriptionError(features, R.string.text_4_13)
        val selectedKind = kind
        if (!nameError && !animalError && selectedKind != null && !ageError && featuresError == null) {
            focusManager.clearFocus()
            val pet = Pet(
                id = existing?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                animal = animal.trim(),
                age = age.toIntOrNull() ?: 0,
                traits = PetTrait.entries.filter { it in traits },
                features = features.trim(),
                photoUri = photoUri,
                kind = selectedKind
            )
            saving = true
            scope.launch {
                AppContainer.pets.save(pet)
                    .onSuccess { onSave(pet.id) }
                    .onFailure { saving = false }
            }
        }
    }

    if (confirmDelete && existing != null) {
        val hasActiveRequest = remember(existing.id) {
            AppContainer.requests.ownerRequests.value.any {
                it.petId == existing.id && it.status != RequestStatus.Completed
            }
        }
        if (hasActiveRequest) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text(stringResource(R.string.text_4_16)) },
                text = { Text(stringResource(R.string.text_4_27, existing.name)) },
                confirmButton = {
                    TextButton(onClick = { confirmDelete = false }) {
                        Text(stringResource(R.string.common_ok))
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text(stringResource(R.string.text_4_16)) },
                text = { Text(stringResource(R.string.text_4_17, existing.name)) },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDelete = false
                        scope.launch {
                            AppContainer.pets.delete(existing.id)
                                .onSuccess { onSave(null) }
                                .onFailure { if (it is PetInUseException) confirmDelete = true }
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
                .padding(horizontal = ScreenHorizontalPadding)
        ) {
            ScreenHeader(
                title = stringResource(if (existing != null) R.string.text_4_15 else R.string.text_4_1),
                onBack = leave,
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

            val scrollState = rememberScrollState()
            PinnedBottomBarLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                dividerVisible = scrollState.canScrollForward,
                bottomBar = {
                    PrimaryButton(
                        text = stringResource(R.string.text_4_7),
                        loading = saving,
                        onClick = { submit() }
                    )
                }
            ) { imeOverlap ->
                val density = LocalDensity.current
                var topContentHeightDp by remember { mutableStateOf(0.dp) }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val stableHeight = maxHeight + imeOverlap
                    val dynamicFeaturesHeight = if (topContentHeightDp > 0.dp) {
                        (stableHeight - topContentHeightDp - ScrollEndGap).coerceAtLeast(FEATURES_MIN_HEIGHT)
                    } else {
                        FEATURES_MIN_HEIGHT
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = ScreenContentInset)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    topContentHeightDp = with(density) { coordinates.size.height.toDp() }
                                }
                        ) {
                            Spacer(Modifier.height(ScreenTopGap))

                            AvatarPicker(
                                name = name,
                                photoUri = photoUri,
                                onPhotoChange = { photoUri = it },
                                forPet = true,
                                hint = stringResource(R.string.text_4_25),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(Modifier.height(FormFieldSpacing))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(FormFieldSpacing),
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
                                        if (!kindManual) kind = recognizedKind(it)
                                        if (kind != null) kindError = false
                                    },
                                    errorText = if (animalError) stringResource(R.string.text_4_11) else null,
                                    placeholder = stringResource(R.string.text_4_19),
                                    keyboardActions = nextField
                                )
                                Column {
                                    FieldLabel(text = stringResource(R.string.text_4_28), required = true)
                                    FieldHint(stringResource(R.string.text_4_29))
                                    SelectableChips(
                                        items = PetKind.entries,
                                        selected = setOfNotNull(kind),
                                        label = { stringResource(it.label) },
                                        onToggle = { item ->
                                            kind = item
                                            kindManual = true
                                            kindError = false
                                        }
                                    )
                                    FieldError(visible = kindError, text = stringResource(R.string.text_4_30))
                                }
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

                            Spacer(Modifier.height(FormFieldSpacing))

                            FieldLabel(text = stringResource(R.string.text_4_6), required = true)
                            FieldHint(stringResource(R.string.text_4_23))

                            PetTraitSelector(
                                selected = traits,
                                onToggle = { trait ->
                                    traits = if (trait in traits) traits - trait else traits + trait
                                }
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
                                .height(dynamicFeaturesHeight)
                        )

                        Spacer(Modifier.height(ScrollEndGap))
                    }
                }
            }
        }
    }
}

private val FEATURES_MIN_HEIGHT = 136.dp

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
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label, required = required)
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
