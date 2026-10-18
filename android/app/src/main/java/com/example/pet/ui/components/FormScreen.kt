package com.example.pet.ui.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val FormFieldSpacing = 12.dp
val FormButtonGap = 24.dp
val FormFooterGap = 16.dp

@Composable
fun FormScreen(
    title: String,
    onBack: () -> Unit,
    button: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenContentInset)
            ) {
                Spacer(Modifier.height(ScreenTopGap))
                Column(
                    verticalArrangement = Arrangement.spacedBy(FormFieldSpacing),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    content = content
                )
                Spacer(Modifier.height(FormButtonGap))
                button()
                if (footer != null) {
                    Spacer(Modifier.height(FormFooterGap))
                    footer()
                }
                Spacer(Modifier.height(ScreenBottomGap))
            }
        }
    }
}

@Composable
fun FormHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    )
}
