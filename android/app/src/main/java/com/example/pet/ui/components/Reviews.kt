package com.example.pet.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.DayMonthYearFormat
import com.example.pet.data.Review
import com.example.pet.data.averageRating
import com.example.pet.ui.theme.PetStar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun StarRow(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    onRate: ((Int) -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (onRate != null) 8.dp else 2.dp),
        modifier = modifier
    ) {
        (1..5).forEach { value ->
            val filled = value <= rating
            val scale by animateFloatAsState(
                targetValue = if (filled && onRate != null) 1.1f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
                label = "star$value"
            )
            val clickModifier = if (onRate != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onRate(value) }
            } else {
                Modifier
            }
            Icon(
                imageVector = if (filled) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (filled) PetStar else MaterialTheme.colorScheme.outline,
                modifier = clickModifier
                    .size(starSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

@Composable
fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .cardSurface()
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(name = review.authorName, size = 40.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = review.authorName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = review.date.format(DayMonthYearFormat),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StarRow(rating = review.rating)
        }
        Text(
            text = review.text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ReviewSummary(reviews: List<Review>, modifier: Modifier = Modifier) {
    val average = reviews.averageRating()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .cardSurface()
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.text_15_2, average),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            StarRow(rating = average.roundToInt())
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.text_15_3, reviews.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(20.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            (5 downTo 1).forEach { stars ->
                val count = reviews.count { it.rating == stars }
                val fraction = if (reviews.isEmpty()) 0f else count.toFloat() / reviews.size
                val animated by animateFloatAsState(targetValue = fraction, label = "bar$stars")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stars.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(14.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animated)
                                .clip(RoundedCornerShape(3.dp))
                                .background(PetStar)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSheet(
    volunteerName: String,
    onSubmit: suspend (rating: Int, text: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var rating by rememberSaveable { mutableIntStateOf(0) }
    var text by rememberSaveable { mutableStateOf("") }
    var ratingError by rememberSaveable { mutableStateOf(false) }
    var textError by rememberSaveable { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.text_15_4),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = volunteerName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            StarRow(
                rating = rating,
                starSize = 40.dp,
                onRate = {
                    rating = it
                    ratingError = false
                }
            )
            if (ratingError) {
                Text(
                    text = stringResource(R.string.text_15_5),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it.take(FormRules.LONG_TEXT_MAX_LENGTH)
                    textError = false
                },
                placeholder = { Text(stringResource(R.string.text_15_6)) },
                isError = textError,
                supportingText = if (textError) {
                    { Text(stringResource(R.string.common_min_length, FormRules.DESCRIPTION_MIN_LENGTH)) }
                } else {
                    null
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = stringResource(R.string.text_15_7),
                loading = sending,
                onClick = {
                    ratingError = rating == 0
                    textError = text.trim().length < FormRules.DESCRIPTION_MIN_LENGTH
                    if (!ratingError && !textError) {
                        scope.launch {
                            sending = true
                            onSubmit(rating, text.trim())
                            sending = false
                            close()
                        }
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}