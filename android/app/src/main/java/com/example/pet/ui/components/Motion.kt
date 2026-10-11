package com.example.pet.ui.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

const val ShortAnimMs = 200

fun revealEnter(clip: Boolean = true): EnterTransition =
    fadeIn(tween(ShortAnimMs)) + expandVertically(animationSpec = tween(ShortAnimMs), clip = clip)

fun revealExit(clip: Boolean = true): ExitTransition =
    fadeOut(tween(ShortAnimMs)) + shrinkVertically(animationSpec = tween(ShortAnimMs), clip = clip)

fun <S> AnimatedContentTransitionScope<S>.fadeThrough(): ContentTransform =
    fadeIn(tween(ShortAnimMs)) togetherWith fadeOut(tween(ShortAnimMs))

@Composable
fun ExpandChevron(expanded: Boolean, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(ShortAnimMs),
        label = "expandChevron"
    )
    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.rotate(rotation)
    )
}
