package com.example.pet.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle

/**
 * "Нет аккаунта? Зарегистрироваться" одной надписью.
 * Перенос (если не влезает на маленьком экране / крупном шрифте)
 * происходит по границе слова, а не буквы, и текст остаётся по центру.
 */
@Composable
fun AuthFooterLink(
    plainText: String,
    linkText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val annotatedText = buildAnnotatedString {
        append("$plainText ")
        pushStringAnnotation(tag = "LINK", annotation = "link")
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(linkText)
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = Color.Gray,
            textAlign = TextAlign.Center
        ),
        modifier = modifier.fillMaxWidth(),
        onClick = { offset ->
            annotatedText.getStringAnnotations("LINK", offset, offset)
                .firstOrNull()?.let { onClick() }
        }
    )
}
