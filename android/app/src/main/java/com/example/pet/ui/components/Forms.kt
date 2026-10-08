package com.example.pet.ui.components

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pet.R

object FormRules {
    const val PHONE_LENGTH = 10
    const val PASSWORD_MIN_LENGTH = 8
    const val CODE_LENGTH = 6
    const val PET_AGE_MAX_DIGITS = 2
    const val DESCRIPTION_MIN_LENGTH = 10
    const val NAME_MAX_LENGTH = 60
    const val EMAIL_MAX_LENGTH = 100
    const val PASSWORD_MAX_LENGTH = 64
    const val SHORT_TEXT_MAX_LENGTH = 40
    const val ADDRESS_DETAILS_MAX_LENGTH = 100
    const val LONG_TEXT_MAX_LENGTH = 1000
    const val DEFAULT_MAX_LENGTH = 200
    val FULL_NAME_WORDS = 2..3

    fun normalizeFullName(value: String): String =
        value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")

    fun fullNameError(value: String): Int? {
        val words = normalizeFullName(value).split(" ").filter { it.isNotEmpty() }
        return if (words.size in FULL_NAME_WORDS) null else R.string.text_3_12
    }

    fun descriptionError(text: String, @StringRes emptyError: Int): Int? = when {
        text.isBlank() -> emptyError
        text.trim().length < DESCRIPTION_MIN_LENGTH -> R.string.common_min_length
        else -> null
    }

    fun isEmailValid(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
}

fun Modifier.clearFocusOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    pointerInput(Unit) {
        detectTapGestures(onTap = { focusManager.clearFocus() })
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    helperText: String? = null,
    maxLength: Int = if (singleLine) FormRules.DEFAULT_MAX_LENGTH else FormRules.LONG_TEXT_MAX_LENGTH
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(maxLength)) },
        placeholder = {
            Text(
                text = placeholder,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null) }
        } else {
            null
        },
        trailingIcon = trailingIcon,
        isError = errorText != null,
        supportingText = when {
            errorText != null -> {
                { Text(errorText) }
            }
            helperText != null -> {
                { Text(helperText) }
            }
            else -> null
        },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    AppTextField(
        maxLength = FormRules.PASSWORD_MAX_LENGTH,
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        leadingIcon = Icons.Default.Lock,
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Crossfade(targetState = visible, label = "passwordEye") { isVisible ->
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = stringResource(
                            if (isVisible) R.string.text_2_14 else R.string.text_2_13
                        )
                    )
                }
            }
        },
        errorText = errorText,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        keyboardActions = keyboardActions,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

fun formatPhone(digits: String): String {
    if (digits.length != FormRules.PHONE_LENGTH || !digits.all(Char::isDigit)) return digits
    return "+7 (${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6, 8)}-${digits.substring(8, 10)}"
}