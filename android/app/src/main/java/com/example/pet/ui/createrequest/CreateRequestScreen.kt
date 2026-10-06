package com.example.pet.ui.createrequest

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.pet.ui.components.toUtcMillis
import com.example.pet.ui.components.utcMillisToLocalDate
import com.example.pet.data.UserRole
import com.example.pet.data.RequestStatus
import com.example.pet.data.PetRequest
import com.example.pet.data.Pet
import com.example.pet.data.AppContainer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import com.example.pet.ui.components.DateRangeDialog
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PetTraitChips
import com.example.pet.ui.components.PinnedBottomBarLayout
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import com.example.pet.ui.components.pressScale
import com.example.pet.ui.components.rememberFutureDateRangePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    requestId: String?,
    onBack: () -> Unit,
    onAddPet: () -> Unit,
    onCreate: () -> Unit,
    pickedAddress: String?,
    onPickedAddressUsed: () -> Unit,
    onPickOnMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val existing = remember(requestId) {
        requestId?.let { id -> AppContainer.requests.ownerRequests.value.firstOrNull { it.id == id } }
    }
    var saving by remember { mutableStateOf(false) }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showPetPicker by rememberSaveable { mutableStateOf(false) }

    val pets by AppContainer.pets.pets.collectAsStateWithLifecycle()
    var selectedPetId by rememberSaveable { mutableStateOf(existing?.petId) }
    val selectedPet = pets.firstOrNull { it.id == selectedPetId }

    var petError by rememberSaveable { mutableStateOf(false) }
    var datesError by rememberSaveable { mutableStateOf(false) }
    var addressError by rememberSaveable { mutableStateOf(false) }
    var commentError by rememberSaveable { mutableStateOf<Int?>(null) }

    val pickerState = rememberFutureDateRangePickerState()
    var datesPrefilled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(existing) {
        if (existing != null && !datesPrefilled) {
            pickerState.setSelection(existing.start.toUtcMillis(), existing.end.toUtcMillis())
            datesPrefilled = true
        }
    }

    var address by rememberSaveable { mutableStateOf(existing?.address.orEmpty()) }

    LaunchedEffect(pickedAddress) {
        if (pickedAddress != null) {
            address = pickedAddress
            addressError = false
            onPickedAddressUsed()
        }
    }
    var comment by rememberSaveable { mutableStateOf(existing?.comment.orEmpty()) }

    val start = pickerState.selectedStartDateMillis
    val end = pickerState.selectedEndDateMillis
    val hasDates = start != null && end != null
    val datesText = if (start != null && end != null) {
        val format = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ru")).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val days = TimeUnit.MILLISECONDS.toDays(end - start).toInt().coerceAtLeast(1)
        stringResource(
            R.string.text_5_13,
            format.format(Date(start)),
            format.format(Date(end)),
            pluralStringResource(R.plurals.common_days_count, days, days)
        )
    } else {
        stringResource(R.string.text_5_6)
    }

    fun submit() {
        petError = selectedPet == null
        datesError = !hasDates
        addressError = address.isBlank()
        commentError = FormRules.descriptionError(comment, R.string.text_5_24)
        if (!petError && !datesError && !addressError && commentError == null && selectedPet != null && start != null && end != null) {
            focusManager.clearFocus()
            val request = PetRequest(
                id = existing?.id ?: UUID.randomUUID().toString(),
                petId = selectedPet.id,
                title = selectedPet.name,
                petInfo = selectedPet.info,
                kind = selectedPet.kind,
                start = utcMillisToLocalDate(start),
                end = utcMillisToLocalDate(end),
                district = existing?.district.orEmpty(),
                address = address.trim(),
                comment = comment.trim(),
                traits = selectedPet.traits,
                features = selectedPet.features,
                status = existing?.status ?: RequestStatus.Open,
                chosenVolunteerId = existing?.chosenVolunteerId,
                ownerName = AppContainer.profiles.profile(UserRole.Owner).value.name
            )
            scope.launch {
                saving = true
                AppContainer.requests.save(request)
                saving = false
                onCreate()
            }
        }
    }

    if (showPetPicker) {
        PetPickerSheet(
            pets = pets,
            selectedPetId = selectedPetId,
            onPetSelected = { pet ->
                selectedPetId = pet.id
                petError = false
            },
            onAddPet = onAddPet,
            onDismiss = { showPetPicker = false }
        )
    }

    if (showDatePicker) {
        DateRangeDialog(
            state = pickerState,
            title = stringResource(R.string.text_5_16),
            onConfirm = {
                showDatePicker = false
                datesError = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

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
                title = stringResource(if (existing != null) R.string.text_5_26 else R.string.text_5_1),
                onBack = onBack
            )

            PinnedBottomBarLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                bottomBar = {
                    PrimaryButton(
                        text = stringResource(if (existing != null) R.string.text_4_7 else R.string.text_5_12),
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
                    val dynamicCommentHeight = if (topContentHeightDp > 0.dp) {
                        (stableHeight - topContentHeightDp - 16.dp).coerceAtLeast(96.dp)
                    } else {
                        96.dp
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

                            PetCard(
                                pet = selectedPet,
                                isError = petError,
                                onClick = {
                                    focusManager.clearFocus()
                                    showPetPicker = true
                                }
                            )
                            ErrorText(
                                visible = petError,
                                text = stringResource(R.string.text_5_19)
                            )

                            Spacer(Modifier.height(16.dp))

                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                SectionLabel(stringResource(R.string.text_5_5))

                                val datesBorder by animateColorAsState(
                                    targetValue = if (datesError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    label = "datesBorder"
                                )
                                val datesInteraction = remember { MutableInteractionSource() }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pressScale(datesInteraction, pressedScale = 0.98f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, datesBorder, RoundedCornerShape(16.dp))
                                        .clickable(interactionSource = datesInteraction, indication = null) {
                                            focusManager.clearFocus()
                                            showDatePicker = true
                                        }
                                        .padding(horizontal = 16.dp, vertical = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = datesText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (hasDates) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                ErrorText(
                                    visible = datesError,
                                    text = stringResource(R.string.text_5_20),
                                    horizontalPadding = 16.dp
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                SectionLabel(stringResource(R.string.text_5_7))
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = {
                                        address = it
                                        addressError = false
                                    },
                                    placeholder = { Text(stringResource(R.string.text_5_8)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            focusManager.clearFocus()
                                            onPickOnMap()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Map,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    isError = addressError,
                                    supportingText = if (addressError) {
                                        { Text(stringResource(R.string.text_5_21)) }
                                    } else {
                                        null
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            SectionLabel(
                                text = stringResource(R.string.text_5_11),
                                startPadding = 12.dp
                            )
                        }

                        val commentErrorText = commentError?.let {
                            stringResource(it, FormRules.DESCRIPTION_MIN_LENGTH)
                        }
                        OutlinedTextField(
                            value = comment,
                            onValueChange = {
                                comment = it
                                commentError = null
                            },
                            placeholder = { Text(stringResource(R.string.text_5_25)) },
                            isError = commentError != null,
                            supportingText = if (commentErrorText != null) {
                                { Text(commentErrorText) }
                            } else {
                                null
                            },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(dynamicCommentHeight)
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PetCard(
    pet: Pet?,
    isError: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
        label = "petCardBorder"
    )
    val interaction = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .pressScale(interaction, pressedScale = 0.98f)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
        )

        AnimatedContent(
            targetState = pet,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "selectedPet",
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) { current ->
            Column {
                Text(
                    text = current?.name ?: stringResource(R.string.text_5_4),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = current?.info ?: stringResource(R.string.text_5_22),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (current != null) {
                    PetTraitChips(
                        traits = current.traits,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = stringResource(if (pet != null) R.string.text_5_15 else R.string.text_5_23),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    startPadding: Dp = 4.dp
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(start = startPadding, bottom = 4.dp)
    )
}

@Composable
private fun ErrorText(
    visible: Boolean,
    text: String,
    horizontalPadding: Dp = 24.dp
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = horizontalPadding, top = 4.dp)
        )
    }
}