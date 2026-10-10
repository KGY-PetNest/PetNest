package com.example.pet.ui.editprofile

import com.example.pet.ui.components.ScreenHorizontalPadding
import com.example.pet.ui.components.icon
import com.example.pet.ui.components.serviceLabel
import com.example.pet.data.CareFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.AcceptedPet
import com.example.pet.data.AppContainer
import com.example.pet.data.HomeConditionType
import com.example.pet.data.MockData
import com.example.pet.data.UserProfile
import com.example.pet.data.UserRole
import com.example.pet.data.repository.AccountExistsException
import com.example.pet.data.toggled
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.AvatarPicker
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.HomeConditionSelector
import com.example.pet.ui.components.PhoneVisualTransformation
import com.example.pet.ui.components.PinnedBottomBarLayout
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SelectableChips
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import com.example.pet.ui.components.label
import kotlinx.coroutines.launch
import com.example.pet.ui.components.rememberLeaveGuard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun EditProfileScreen(
    role: UserRole,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    becomeVolunteer: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val profile = remember(role, becomeVolunteer) {
        AppContainer.profiles.profile(if (becomeVolunteer) UserRole.Owner else role).value
    }
    val volunteer = remember(role) {
        if (role == UserRole.Volunteer) {
            AppContainer.volunteers.volunteers.value.firstOrNull { it.id == MockData.CURRENT_VOLUNTEER_ID }
        } else {
            null
        }
    }
    val filled = volunteer.takeUnless { becomeVolunteer }
    val startAvatar = filled?.avatarUri ?: profile.avatarUri
    val startExperience = filled?.experience.orEmpty()
    val startAbout = filled?.about.orEmpty()
    val startHome = filled?.homeConditions?.toSet() ?: emptySet()
    val startAccepted = filled?.acceptedPets?.toSet() ?: emptySet()
    val startFormats = filled?.formats?.toSet() ?: emptySet()

    var name by rememberSaveable { mutableStateOf(profile.name) }
    var avatarUri by rememberSaveable { mutableStateOf(startAvatar) }
    var phone by rememberSaveable { mutableStateOf(profile.phone) }
    var email by rememberSaveable { mutableStateOf(profile.email) }
    var experience by rememberSaveable { mutableStateOf(startExperience) }
    var about by rememberSaveable { mutableStateOf(startAbout) }
    var homeConditions by rememberSaveable { mutableStateOf(startHome) }
    var acceptedPets by rememberSaveable { mutableStateOf(startAccepted) }
    var formats by rememberSaveable { mutableStateOf(startFormats) }

    var nameError by rememberSaveable { mutableStateOf<Int?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<Int?>(null) }
    var emailError by rememberSaveable { mutableStateOf<Int?>(null) }
    var experienceError by rememberSaveable { mutableStateOf<Int?>(null) }
    var aboutError by rememberSaveable { mutableStateOf<Int?>(null) }
    var acceptedError by rememberSaveable { mutableStateOf(false) }
    var formatsError by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf(false) }

    val hasChanges = name != profile.name ||
            avatarUri != startAvatar ||
            phone != profile.phone ||
            email != profile.email ||
            experience != startExperience ||
            about != startAbout ||
            homeConditions != startHome ||
            acceptedPets != startAccepted ||
            formats != startFormats
    val leave = rememberLeaveGuard(hasChanges = hasChanges && !saving, onLeave = onBack)

    var confirmEmail by rememberSaveable { mutableStateOf(false) }

    fun submit(emailConfirmed: Boolean = false) {
        if (saving) return
        saveError = false
        nameError = FormRules.fullNameError(name)
        phoneError = if (phone.length != FormRules.PHONE_LENGTH) R.string.text_3_6 else null
        emailError = when {
            email.isBlank() -> R.string.text_2_10
            !FormRules.isEmailValid(email) -> R.string.text_2_11
            else -> null
        }
        if (volunteer != null) {
            experienceError = if (experience.isBlank()) R.string.text_17_6 else null
            aboutError = FormRules.descriptionError(about, R.string.text_17_7)
            acceptedError = acceptedPets.isEmpty()
            formatsError = formats.isEmpty()
        }
        val hasErrors = listOf(nameError, phoneError, emailError, experienceError, aboutError).any { it != null } ||
                acceptedError || formatsError
        if (hasErrors) return
        if (!emailConfirmed && email.trim() != profile.email) {
            confirmEmail = true
            return
        }

        focusManager.clearFocus()
        scope.launch {
            saving = true
            val emailResult = if (email.trim() != profile.email) {
                AppContainer.auth.changeEmail(email.trim())
            } else {
                Result.success(Unit)
            }
            if (emailResult.isFailure) {
                saving = false
                if (emailResult.exceptionOrNull() is AccountExistsException) {
                    emailError = R.string.text_17_20
                } else {
                    saveError = true
                }
                return@launch
            }
            val profileResult = AppContainer.profiles.updateProfile(
                role,
                UserProfile(
                    name = FormRules.normalizeFullName(name),
                    phone = phone,
                    email = email.trim(),
                    avatarUri = avatarUri
                )
            )
            val otherRole = if (role == UserRole.Owner) UserRole.Volunteer else UserRole.Owner
            if (profileResult.isSuccess && email.trim() != profile.email) {
                val other = AppContainer.profiles.profile(otherRole).value
                AppContainer.profiles.updateProfile(otherRole, other.copy(email = email.trim()))
            }
            val volunteerResult = if (volunteer != null && profileResult.isSuccess) {
                AppContainer.volunteers.update(
                    volunteer.copy(
                        name = FormRules.normalizeFullName(name),
                        phone = phone,
                        avatarUri = avatarUri,
                        experience = experience.trim(),
                        about = about.trim(),
                        homeConditions = if (CareFormat.AtVolunteer in formats) {
                            HomeConditionType.entries.filter { it in homeConditions }
                        } else {
                            emptyList()
                        },
                        acceptedPets = AcceptedPet.entries.filter { it in acceptedPets },
                        formats = CareFormat.entries.filter { it in formats }
                    )
                )
            } else {
                profileResult
            }
            val result = if (becomeVolunteer && volunteerResult.isSuccess) {
                AppContainer.auth.addRole(UserRole.Volunteer)
            } else {
                volunteerResult
            }
            saving = false
            if (result.isSuccess) onSaved() else saveError = true
        }
    }

    val nextField = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    if (confirmEmail) {
        AlertDialog(
            onDismissRequest = { confirmEmail = false },
            title = { Text(stringResource(R.string.text_17_15)) },
            text = { Text(stringResource(R.string.text_17_16, email.trim())) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmEmail = false
                        submit(emailConfirmed = true)
                    }
                ) {
                    Text(stringResource(R.string.text_4_7))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmEmail = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
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
                .padding(horizontal = ScreenHorizontalPadding)
        ) {
            ScreenHeader(
                title = stringResource(if (becomeVolunteer) R.string.text_17_17 else R.string.text_17_1),
                onBack = leave
            )

            val scrollState = rememberScrollState()
            PinnedBottomBarLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                dividerVisible = scrollState.canScrollForward,
                bottomBar = {
                    if (saveError) {
                        Text(
                            text = stringResource(R.string.common_request_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    PrimaryButton(
                        text = stringResource(if (becomeVolunteer) R.string.common_become_volunteer else R.string.text_4_7),
                        loading = saving,
                        onClick = { submit() }
                    )
                    Spacer(Modifier.height(32.dp))
                }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 8.dp)
                        .animateContentSize()
                ) {
                    if (becomeVolunteer) {
                        Text(
                            text = stringResource(R.string.text_17_18),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    AvatarPicker(
                        name = name,
                        photoUri = avatarUri,
                        onPhotoChange = { avatarUri = it },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    AppTextField(
                        value = name,
                        maxLength = FormRules.NAME_MAX_LENGTH,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        placeholder = stringResource(R.string.text_3_2),
                        leadingIcon = Icons.Default.Person,
                        errorText = nameError?.let { stringResource(it) },
                        keyboardActions = nextField
                    )
                    AppTextField(
                        value = phone,
                        onValueChange = { input ->
                            val digits = input.filter(Char::isDigit)
                            val local = if (
                                digits.length > FormRules.PHONE_LENGTH &&
                                (digits.startsWith("7") || digits.startsWith("8"))
                            ) {
                                digits.drop(1)
                            } else {
                                digits
                            }
                            phone = local.take(FormRules.PHONE_LENGTH)
                            phoneError = null
                        },
                        placeholder = stringResource(R.string.text_3_3),
                        leadingIcon = Icons.Default.Phone,
                        visualTransformation = PhoneVisualTransformation(),
                        errorText = phoneError?.let { stringResource(it) },
                        keyboardType = KeyboardType.Phone,
                        keyboardActions = nextField
                    )
                    AppTextField(
                        value = email,
                        onValueChange = {
                            email = it.trim()
                            emailError = null
                        },
                        maxLength = FormRules.EMAIL_MAX_LENGTH,
                        placeholder = stringResource(R.string.text_2_4),
                        leadingIcon = Icons.Default.Email,
                        errorText = emailError?.let { stringResource(it) },
                        keyboardType = KeyboardType.Email,
                        imeAction = if (volunteer != null) ImeAction.Next else ImeAction.Done,
                        keyboardActions = if (volunteer != null) nextField else KeyboardActions(onDone = { submit() })
                    )

                    if (volunteer != null) {
                        FieldTitle(stringResource(R.string.text_17_2))
                        FieldHint(stringResource(R.string.text_17_19))
                        AppTextField(
                            value = experience,
                            onValueChange = {
                                experience = it
                                experienceError = null
                            },
                            placeholder = stringResource(R.string.text_17_8),
                            leadingIcon = Icons.Default.Work,
                            errorText = experienceError?.let { stringResource(it) },
                            keyboardActions = nextField
                        )

                        FieldTitle(stringResource(R.string.text_13_4))
                        AppTextField(
                            value = about,
                            onValueChange = {
                                about = it
                                aboutError = null
                            },
                            placeholder = stringResource(R.string.text_17_9),
                            errorText = aboutError?.let { stringResource(it, FormRules.DESCRIPTION_MIN_LENGTH) },
                            imeAction = ImeAction.Default,
                            singleLine = false,
                            modifier = Modifier.height(140.dp)
                        )

                        FieldTitle(stringResource(R.string.text_13_18))
                        SelectableChips(
                            items = CareFormat.entries,
                            selected = formats,
                            label = { stringResource(it.serviceLabel) },
                            icon = { it.icon },
                            onToggle = { item ->
                                formats = if (item in formats) formats - item else formats + item
                                formatsError = false
                            }
                        )
                        AnimatedVisibility(visible = formatsError) {
                            Text(
                                text = stringResource(R.string.text_17_10),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        AnimatedVisibility(visible = CareFormat.AtVolunteer in formats) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                FieldTitle(stringResource(R.string.text_13_5))
                                FieldHint(stringResource(R.string.text_17_14))
                                HomeConditionSelector(
                                    selected = homeConditions,
                                    onToggle = { item -> homeConditions = homeConditions.toggled(item) }
                                )
                            }
                        }

                        FieldTitle(stringResource(R.string.text_13_6))
                        SelectableChips(
                            items = AcceptedPet.entries,
                            selected = acceptedPets,
                            label = { stringResource(it.label) },
                            onToggle = { item ->
                                acceptedPets = if (item in acceptedPets) acceptedPets - item else acceptedPets + item
                                acceptedError = false
                            }
                        )
                        AnimatedVisibility(visible = acceptedError) {
                            Text(
                                text = stringResource(R.string.text_17_10),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FieldTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun FieldHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}
