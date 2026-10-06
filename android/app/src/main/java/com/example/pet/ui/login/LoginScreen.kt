package com.example.pet.ui.login

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch
import com.example.pet.data.AppContainer
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.material3.TextButton
import com.example.pet.data.UserRole
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.AuthFooterLink
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PasswordField
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onSuccess: (UserRole) -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    var selectedRole by rememberSaveable { mutableIntStateOf(0) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var emailError by rememberSaveable { mutableStateOf<Int?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<Int?>(null) }

    fun submit() {
        emailError = when {
            email.isBlank() -> R.string.text_2_10
            !FormRules.isEmailValid(email) -> R.string.text_2_11
            else -> null
        }
        passwordError = if (password.isEmpty()) R.string.text_2_12 else null

        if (emailError == null && passwordError == null) {
            focusManager.clearFocus()
            val role = if (selectedRole == 1) UserRole.Volunteer else UserRole.Owner
            scope.launch {
                loading = true
                val result = AppContainer.auth.login(email.trim(), password, role)
                loading = false
                if (result.isSuccess) {
                    onSuccess(role)
                } else {
                    passwordError = R.string.text_2_15
                }
            }
        }
    }

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
                text = stringResource(R.string.text_2_1),
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
                    value = email,
                    onValueChange = {
                        email = it.trim()
                        emailError = null
                    },
                    placeholder = stringResource(R.string.text_2_4),
                    leadingIcon = Icons.Default.Email,
                    errorText = emailError?.let { stringResource(it) },
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                PasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    placeholder = stringResource(R.string.text_2_5),
                    errorText = passwordError?.let { stringResource(it) },
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
            }

            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.text_2_16))
            }

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                text = stringResource(R.string.text_2_6),
                loading = loading,
                onClick = { submit() }
            )

            Spacer(Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthFooterLink(
                    plainText = stringResource(R.string.text_2_7),
                    linkText = stringResource(R.string.text_2_8),
                    onClick = onRegisterClick
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.text_2_9),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}