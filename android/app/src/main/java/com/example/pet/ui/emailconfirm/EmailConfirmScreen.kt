package com.example.pet.ui.emailconfirm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.AuthFooterLink
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.clearFocusOnTap
import kotlinx.coroutines.delay

private const val RESEND_SECONDS = 60

@Composable
fun EmailConfirmScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null
) {
    val focusManager = LocalFocusManager.current

    var code by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(RESEND_SECONDS) }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    fun submit() {
        if (code.length == FormRules.CODE_LENGTH) {
            focusManager.clearFocus()
            onSuccess()
        } else {
            isError = true
        }
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
            ScreenHeader(title = stringResource(R.string.text_6_1), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    text = message ?: stringResource(R.string.text_6_2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(24.dp))

                AppTextField(
                    value = code,
                    onValueChange = {
                        code = it.filter(Char::isDigit).take(FormRules.CODE_LENGTH)
                        isError = false
                        if (code.length == FormRules.CODE_LENGTH) focusManager.clearFocus()
                    },
                    placeholder = stringResource(R.string.text_6_3),
                    leadingIcon = Icons.Default.Email,
                    errorText = if (isError) stringResource(R.string.text_6_4) else null,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )

                Spacer(Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(R.string.text_6_5),
                    onClick = { submit() }
                )

                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = secondsLeft > 0,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "resend",
                    modifier = Modifier.fillMaxWidth()
                ) { waiting ->
                    if (waiting) {
                        Text(
                            text = stringResource(R.string.text_6_8, secondsLeft),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        AuthFooterLink(
                            plainText = stringResource(R.string.text_6_6),
                            linkText = stringResource(R.string.text_6_7),
                            onClick = {
                                onResend()
                                secondsLeft = RESEND_SECONDS
                            }
                        )
                    }
                }
            }
        }
    }
}