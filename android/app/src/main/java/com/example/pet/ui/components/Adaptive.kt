package com.example.pet.ui.components

import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
fun BottomInsetsPane(
    includeIme: Boolean = true,
    content: @Composable () -> Unit
) {
    val insets = if (includeIme) {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
    } else {
        WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(insets)
    ) {
        content()
    }
}

val PinnedBarGap = 12.dp

@Composable
fun PinnedBottomBarLayout(
    modifier: Modifier = Modifier,
    dividerVisible: Boolean = false,
    bottomBar: @Composable ColumnScope.() -> Unit,
    content: @Composable BoxScope.(imeOverlap: Dp) -> Unit
) {
    val density = LocalDensity.current
    var barHeightPx by remember { mutableIntStateOf(0) }

    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navBottomPx = WindowInsets.navigationBars.getBottom(density)
    val overlapPx = (imeBottomPx - navBottomPx - barHeightPx).coerceAtLeast(0)
    val overlap = with(density) { overlapPx.toDp() }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = overlap)
        ) {
            content(overlap)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { barHeightPx = it.height }
        ) {
            PinnedBarDivider(visible = dividerVisible)
            Spacer(Modifier.height(PinnedBarGap))
            bottomBar()
        }
    }
}

val ScreenHorizontalPadding = 12.dp
val ScreenContentInset = 4.dp
val ScreenTextPadding = ScreenHorizontalPadding + ScreenContentInset

fun Modifier.bleedHorizontally(amount: Dp): Modifier = layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val width = if (constraints.hasBoundedWidth) constraints.maxWidth + extra * 2 else constraints.minWidth
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    val ownWidth = (width - extra * 2).coerceAtLeast(0)
    layout(ownWidth, placeable.height) {
        placeable.place(-extra, 0)
    }
}

@Composable
fun PinnedBarDivider(visible: Boolean, modifier: Modifier = Modifier) {
    val dividerAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, label = "pinnedBarDivider")
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier
            .bleedHorizontally(ScreenHorizontalPadding)
            .graphicsLayer { alpha = dividerAlpha }
    )
}