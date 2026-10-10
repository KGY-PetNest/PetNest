package com.example.pet.ui.password

import com.example.pet.ui.components.ScreenContentInset
import com.example.pet.ui.components.ScreenHorizontalPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PasswordField
import com.example.pet.ui.components.PhoneVisualTransformation
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import com.example.pet.ui.components.formatPhone
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.pet.data.repository.WrongPasswordException
import com.example.pet.ui.components.showRequestError

@Composable
private fun PasswordScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
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
            ScreenHeader(title = title, onBack = onBack)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
                    .animateContentSize(),
                content = content
            )
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenContentInset)
    )
}

@Composable
private fun passwordErrorText(error: Int?): String? = error?.let {
    if (it == R.string.common_min_length) stringResource(it, FormRules.PASSWORD_MIN_LENGTH) else stringResource(it)
}

private fun newPasswordError(password: String): Int? = when {
    password.isEmpty() -> R.string.text_2_12
    password.length < FormRules.PASSWORD_MIN_LENGTH -> R.string.common_min_length
    else -> null
}

private fun repeatError(password: String, repeat: String): Int? = when {
    repeat.isEmpty() -> R.string.text_3_11
    repeat != password -> R.string.text_3_8
    else -> null
}

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var current by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var currentError by rememberSaveable { mutableStateOf<Int?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<Int?>(null) }
    var repeatErrorRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun submit() {
        if (saving) return
        currentError = if (current.isEmpty()) R.string.text_18_4 else null
        passwordError = newPasswordError(password) ?: if (password == current) R.string.text_18_5 else null
        repeatErrorRes = repeatError(password, repeat)
        if (currentError != null || passwordError != null || repeatErrorRes != null) return
        focusManager.clearFocus()
        scope.launch {
            saving = true
            val result = AppContainer.auth.changePassword(current, password)
            saving = false
            if (result.isSuccess) {
                Toast.makeText(context, R.string.text_18_8, Toast.LENGTH_SHORT).show()
                onChanged()
            } else if (result.exceptionOrNull() is WrongPasswordException) {
                currentError = R.string.text_18_6
            } else {
                showRequestError(context)
            }
        }
    }

    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    PasswordScaffold(title = stringResource(R.string.text_16_3), onBack = onBack, modifier = modifier) {
        PasswordField(
            value = current,
            onValueChange = {
                current = it
                currentError = null
            },
            placeholder = stringResource(R.string.text_18_1),
            errorText = currentError?.let { stringResource(it) },
            imeAction = ImeAction.Next,
            keyboardActions = next
        )
        PasswordField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
                repeatErrorRes = null
            },
            placeholder = stringResource(R.string.text_18_2),
            errorText = passwordErrorText(passwordError),
            imeAction = ImeAction.Next,
            keyboardActions = next
        )
        PasswordField(
            value = repeat,
            onValueChange = {
                repeat = it
                repeatErrorRes = null
            },
            placeholder = stringResource(R.string.text_3_7),
            errorText = repeatErrorRes?.let { stringResource(it) },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { submit() })
        )
        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.text_18_3))
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = stringResource(R.string.text_18_7),
            loading = saving,
            onClick = { submit() }
        )
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onCodeSent: (target: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var channel by rememberSaveable { mutableIntStateOf(0) }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<Int?>(null) }
    var sending by remember { mutableStateOf(false) }

    fun submit() {
        if (sending) return
        error = if (channel == 0) {
            when {
                email.isBlank() -> R.string.text_2_10
                !FormRules.isEmailValid(email) -> R.string.text_2_11
                else -> null
            }
        } else {
            if (phone.length != FormRules.PHONE_LENGTH) R.string.text_3_6 else null
        }
        if (error != null) return
        val target = if (channel == 0) email.trim() else formatPhone(phone)
        focusManager.clearFocus()
        scope.launch {
            sending = true
            val result = AppContainer.auth.requestPasswordReset(target)
            sending = false
            if (result.isSuccess) {
                onCodeSent(target)
            } else {
                error = R.string.text_19_5
            }
        }
    }

    PasswordScaffold(title = stringResource(R.string.text_19_1), onBack = onBack, modifier = modifier) {
        Hint(stringResource(R.string.text_19_2))
        Spacer(Modifier.height(4.dp))
        SegmentedToggle(
            options = listOf(stringResource(R.string.text_2_4), stringResource(R.string.text_19_3)),
            selectedIndex = channel,
            onSelect = {
                channel = it
                error = null
            }
        )
        AnimatedContent(
            targetState = channel,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "resetChannel"
        ) { selected ->
            if (selected == 0) {
                AppTextField(
                    value = email,
                    maxLength = FormRules.EMAIL_MAX_LENGTH,
                    onValueChange = {
                        email = it.trim()
                        error = null
                    },
                    placeholder = stringResource(R.string.text_2_4),
                    leadingIcon = Icons.Default.Email,
                    errorText = error?.let { stringResource(it) },
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
            } else {
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
                        error = null
                    },
                    placeholder = stringResource(R.string.text_3_3),
                    leadingIcon = Icons.Default.Phone,
                    visualTransformation = PhoneVisualTransformation(),
                    errorText = error?.let { stringResource(it) },
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = stringResource(R.string.text_19_4),
            loading = sending,
            onClick = { submit() }
        )
    }
}

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf<Int?>(null) }
    var repeatErrorRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun submit() {
        if (saving) return
        passwordError = newPasswordError(password)
        repeatErrorRes = repeatError(password, repeat)
        if (passwordError != null || repeatErrorRes != null) return
        focusManager.clearFocus()
        scope.launch {
            saving = true
            val result = AppContainer.auth.resetPassword(password)
            saving = false
            if (result.isSuccess) {
                onDone()
            } else {
                passwordError = R.string.text_19_5
            }
        }
    }

    PasswordScaffold(title = stringResource(R.string.text_20_1), onBack = onBack, modifier = modifier) {
        Hint(stringResource(R.string.text_20_2))
        Spacer(Modifier.height(4.dp))
        PasswordField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
                repeatErrorRes = null
            },
            placeholder = stringResource(R.string.text_18_2),
            errorText = passwordErrorText(passwordError),
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        PasswordField(
            value = repeat,
            onValueChange = {
                repeat = it
                repeatErrorRes = null
            },
            placeholder = stringResource(R.string.text_3_7),
            errorText = repeatErrorRes?.let { stringResource(it) },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { submit() })
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = stringResource(R.string.text_18_7),
            loading = saving,
            onClick = { submit() }
        )
    }
}