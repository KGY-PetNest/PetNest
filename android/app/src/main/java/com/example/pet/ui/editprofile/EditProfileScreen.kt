package com.example.pet.ui.editprofile

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
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PhoneVisualTransformation
import com.example.pet.ui.components.PinnedBottomBarLayout
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SelectableChips
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import com.example.pet.ui.components.label
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    role: UserRole,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val profile = remember(role) { AppContainer.profiles.profile(role).value }
    val volunteer = remember(role) {
        if (role == UserRole.Volunteer) {
            AppContainer.volunteers.volunteers.value.firstOrNull { it.id == MockData.CURRENT_VOLUNTEER_ID }
        } else {
            null
        }
    }

    var name by rememberSaveable { mutableStateOf(profile.name) }
    var phone by rememberSaveable { mutableStateOf(profile.phone) }
    var email by rememberSaveable { mutableStateOf(profile.email) }
    var experience by rememberSaveable { mutableStateOf(volunteer?.experience.orEmpty()) }
    var about by rememberSaveable { mutableStateOf(volunteer?.about.orEmpty()) }
    var homeConditions by rememberSaveable { mutableStateOf(volunteer?.homeConditions?.toSet() ?: emptySet()) }
    var acceptedPets by rememberSaveable { mutableStateOf(volunteer?.acceptedPets?.toSet() ?: emptySet()) }

    var nameError by rememberSaveable { mutableStateOf<Int?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<Int?>(null) }
    var emailError by rememberSaveable { mutableStateOf<Int?>(null) }
    var experienceError by rememberSaveable { mutableStateOf<Int?>(null) }
    var aboutError by rememberSaveable { mutableStateOf<Int?>(null) }
    var acceptedError by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    fun submit() {
        nameError = if (name.isBlank()) R.string.text_3_9 else null
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
        }
        val hasErrors = listOf(nameError, phoneError, emailError, experienceError, aboutError).any { it != null } ||
                acceptedError
        if (hasErrors) return

        focusManager.clearFocus()
        scope.launch {
            saving = true
            AppContainer.profiles.updateProfile(
                role,
                UserProfile(name = name.trim(), phone = phone, email = email.trim())
            )
            if (volunteer != null) {
                AppContainer.volunteers.update(
                    volunteer.copy(
                        name = name.trim(),
                        experience = experience.trim(),
                        about = about.trim(),
                        homeConditions = HomeConditionType.entries.filter { it in homeConditions },
                        acceptedPets = AcceptedPet.entries.filter { it in acceptedPets }
                    )
                )
            }
            saving = false
            onSaved()
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
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(title = stringResource(R.string.text_17_1), onBack = onBack)

            PinnedBottomBarLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                bottomBar = {
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(
                        text = stringResource(R.string.text_4_7),
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
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                        .animateContentSize()
                ) {
                    AppTextField(
                        value = name,
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
                        placeholder = stringResource(R.string.text_2_4),
                        leadingIcon = Icons.Default.Email,
                        errorText = emailError?.let { stringResource(it) },
                        keyboardType = KeyboardType.Email,
                        imeAction = if (volunteer != null) ImeAction.Next else ImeAction.Done,
                        keyboardActions = if (volunteer != null) nextField else KeyboardActions(onDone = { submit() })
                    )

                    if (volunteer != null) {
                        FieldTitle(stringResource(R.string.text_17_2))
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

                        FieldTitle(stringResource(R.string.text_13_5))
                        SelectableChips(
                            items = HomeConditionType.entries,
                            selected = homeConditions,
                            label = { stringResource(it.label) },
                            onToggle = { item ->
                                homeConditions = if (item in homeConditions) homeConditions - item else homeConditions + item
                            }
                        )

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

                    Spacer(Modifier.height(8.dp))
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