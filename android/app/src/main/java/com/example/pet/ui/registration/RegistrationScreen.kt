package com.example.pet.ui.registration

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.AuthFooterLink
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PasswordField
import com.example.pet.ui.components.PhoneVisualTransformation
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.PrivacyPolicyLink
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import com.example.pet.data.AppContainer
import com.example.pet.data.UserRole
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onLoginClick: () -> Unit,
    onSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var submitError by rememberSaveable { mutableStateOf(false) }

    var selectedRole by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordRepeat by rememberSaveable { mutableStateOf("") }

    var nameError by rememberSaveable { mutableStateOf<Int?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<Int?>(null) }
    var emailError by rememberSaveable { mutableStateOf<Int?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<Int?>(null) }
    var passwordRepeatError by rememberSaveable { mutableStateOf<Int?>(null) }

    fun submit() {
        if (loading) return
        submitError = false
        nameError = FormRules.fullNameError(name)
        phoneError = if (phone.length != FormRules.PHONE_LENGTH) R.string.text_3_6 else null
        emailError = when {
            email.isBlank() -> R.string.text_2_10
            !FormRules.isEmailValid(email) -> R.string.text_2_11
            else -> null
        }
        passwordError = when {
            password.isEmpty() -> R.string.text_2_12
            password.length < FormRules.PASSWORD_MIN_LENGTH -> R.string.common_min_length
            else -> null
        }
        passwordRepeatError = when {
            passwordRepeat.isEmpty() -> R.string.text_3_11
            passwordRepeat != password -> R.string.text_3_8
            else -> null
        }

        val hasErrors = listOf(nameError, phoneError, emailError, passwordError, passwordRepeatError)
            .any { it != null }
        if (!hasErrors) {
            focusManager.clearFocus()
            val role = if (selectedRole == 1) UserRole.Volunteer else UserRole.Owner
            scope.launch {
                loading = true
                val result = AppContainer.auth.register(
                    name = FormRules.normalizeFullName(name),
                    phone = phone,
                    email = email.trim(),
                    password = password,
                    role = role
                )
                loading = false
                if (result.isSuccess) onSuccess(role) else submitError = true
            }
        }
    }

    val nextField = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    Box(
        modifier = modifier
            .fillMaxSize()
            .clearFocusOnTap(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .adaptiveContentWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.text_3_1),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            SegmentedToggle(
                options = listOf(
                    stringResource(R.string.text_2_2),
                    stringResource(R.string.text_2_3)
                ),
                selectedIndex = selectedRole,
                onSelect = { selectedRole = it }
            )

            Spacer(Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.animateContentSize()
            ) {
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
                    maxLength = FormRules.EMAIL_MAX_LENGTH,
                    onValueChange = {
                        email = it.trim()
                        emailError = null
                    },
                    placeholder = stringResource(R.string.text_2_4),
                    leadingIcon = Icons.Default.Email,
                    errorText = emailError?.let { stringResource(it) },
                    keyboardType = KeyboardType.Email,
                    keyboardActions = nextField
                )

                PasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                        passwordRepeatError = null
                    },
                    placeholder = stringResource(R.string.text_2_5),
                    errorText = passwordError?.let { error ->
                        if (error == R.string.common_min_length) {
                            stringResource(error, FormRules.PASSWORD_MIN_LENGTH)
                        } else {
                            stringResource(error)
                        }
                    },
                    imeAction = ImeAction.Next,
                    keyboardActions = nextField
                )

                PasswordField(
                    value = passwordRepeat,
                    onValueChange = {
                        passwordRepeat = it
                        passwordRepeatError = null
                    },
                    placeholder = stringResource(R.string.text_3_7),
                    errorText = passwordRepeatError?.let { stringResource(it) },
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
            }

            Spacer(Modifier.height(24.dp))

            if (submitError) {
                Text(
                    text = stringResource(R.string.common_request_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            PrimaryButton(
                text = stringResource(R.string.text_3_4),
                loading = loading,
                onClick = { submit() }
            )

            Spacer(Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthFooterLink(
                    plainText = stringResource(R.string.text_3_5),
                    linkText = stringResource(R.string.text_2_6),
                    onClick = onLoginClick
                )

                Spacer(Modifier.height(4.dp))

                PrivacyPolicyLink()
            }
        }
    }
}