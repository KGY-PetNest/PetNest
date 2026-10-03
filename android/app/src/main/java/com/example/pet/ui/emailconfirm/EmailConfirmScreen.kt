package com.example.pet.ui.emailconfirm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.AuthFooterLink
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import kotlinx.coroutines.delay

private const val CODE_LENGTH = 6
private const val RESEND_SECONDS = 60

@Composable
fun EmailConfirmScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var code by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(RESEND_SECONDS) }

    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 640.dp)
                .fillMaxWidth()
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
                    text = stringResource(R.string.text_6_2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.filter(Char::isDigit).take(CODE_LENGTH)
                        isError = false
                    },
                    placeholder = { Text(stringResource(R.string.text_6_3)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    isError = isError,
                    supportingText = { if (isError) Text(stringResource(R.string.text_6_4)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                Spacer(Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(R.string.text_6_5),
                    height = 56.dp,
                    fontSize = 16.sp,
                    onClick = {
                        if (code.length == CODE_LENGTH) onSuccess() else isError = true
                    }
                )

                Spacer(Modifier.height(16.dp))

                if (secondsLeft > 0) {
                    Text(
                        text = stringResource(R.string.text_6_8, secondsLeft),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
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