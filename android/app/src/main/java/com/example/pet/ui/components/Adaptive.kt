package com.example.pet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val COMPACT_MAX_WIDTH = 640.dp
private val EXPANDED_MAX_WIDTH = 720.dp
private const val COMPACT_BREAKPOINT_DP = 600

fun Modifier.adaptiveContentWidth(
    compactMaxWidth: Dp = COMPACT_MAX_WIDTH,
    expandedMaxWidth: Dp = EXPANDED_MAX_WIDTH
): Modifier = composed {
    val isCompact = LocalConfiguration.current.screenWidthDp < COMPACT_BREAKPOINT_DP
    this
        .widthIn(max = if (isCompact) compactMaxWidth else expandedMaxWidth)
        .fillMaxWidth()
}

@Composable
fun AdaptivePane(
    modifier: Modifier = Modifier,
    compactMaxWidth: Dp = COMPACT_MAX_WIDTH,
    expandedMaxWidth: Dp = EXPANDED_MAX_WIDTH,
    content: @Composable BoxScope.() -> Unit
) {
    val isCompact = LocalConfiguration.current.screenWidthDp < COMPACT_BREAKPOINT_DP

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = if (isCompact) compactMaxWidth else expandedMaxWidth)
                .fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun BottomInsetsPane(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
    ) {
        content()
    }
}