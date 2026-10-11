package com.example.pet.ui.emailconfirm

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.pet.R
import com.example.pet.ui.components.AppTextField
import com.example.pet.ui.components.AuthFooterLink
import com.example.pet.ui.components.FormHint
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.FormScreen
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.fadeThrough
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RESEND_SECONDS = 60

@Composable
fun EmailConfirmScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    verify: (suspend (String) -> Result<Unit>)? = null,
    message: String? = null
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }

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
        if (checking) return
        if (code.length != FormRules.CODE_LENGTH) {
            isError = true
            return
        }
        focusManager.clearFocus()
        val check = verify
        if (check == null) {
            onSuccess()
            return
        }
        scope.launch {
            checking = true
            val result = check(code)
            checking = false
            if (result.isSuccess) onSuccess() else isError = true
        }
    }

    FormScreen(
        title = stringResource(R.string.text_6_1),
        onBack = onBack,
        modifier = modifier,
        button = {
            PrimaryButton(
                text = stringResource(R.string.text_6_5),
                loading = checking,
                onClick = { submit() }
            )
        },
        footer = {
            AnimatedContent(
                targetState = secondsLeft > 0,
                transitionSpec = { fadeThrough() },
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
    ) {
        FormHint(message ?: stringResource(R.string.text_6_2))
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
    }
}
