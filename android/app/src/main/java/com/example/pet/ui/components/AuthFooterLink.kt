package com.example.pet.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp

@Composable
fun AuthFooterLink(
    plainText: String,
    linkText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    suffixText: String = ""
) {
    Text(
        text = buildAnnotatedString {
            append("$plainText ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = "link",
                    styles = TextLinkStyles(SpanStyle(color = MaterialTheme.colorScheme.primary)),
                    linkInteractionListener = { onClick() }
                )
            ) { append(linkText) }
            append(suffixText)
        },
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        ),
        modifier = modifier.fillMaxWidth()
    )
}