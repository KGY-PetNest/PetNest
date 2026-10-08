package com.example.pet.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.example.pet.R

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

@Composable
fun PrivacyPolicyLink(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val url = stringResource(R.string.privacy_policy_url)
    if (url.isBlank()) return
    Text(
        text = stringResource(R.string.text_2_9),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.clickable {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
        }
    )
}
