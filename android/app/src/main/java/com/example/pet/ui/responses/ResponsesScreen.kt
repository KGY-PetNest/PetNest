package com.example.pet.ui.responses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.MockData
import com.example.pet.data.Volunteer
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.RatingLabel
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.pressScale

@Composable
fun ResponsesScreen(
    requestId: String,
    onBack: () -> Unit,
    onVolunteerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val request = MockData.request(requestId) ?: MockData.ownerRequests.first()
    val responses = remember(requestId) { MockData.responsesFor(requestId) }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    // TODO: хранить избранное и выбор на сервере
    var favorites by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var selectedVolunteerId by rememberSaveable { mutableStateOf<String?>(null) }

    val shown = if (tab == 0) responses else responses.filter { it.id in favorites }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(title = stringResource(R.string.text_14_1), onBack = onBack)

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .cardSurface()
                    .padding(12.dp)
            ) {
                PetThumbnail(size = 56.dp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = request.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = request.petInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconLine(icon = Icons.Default.DateRange, text = request.dates)
                }
            }

            Spacer(Modifier.height(16.dp))

            SegmentedToggle(
                options = listOf(
                    stringResource(R.string.text_14_2),
                    stringResource(R.string.text_14_3)
                ),
                selectedIndex = tab,
                onSelect = { tab = it }
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(shown, key = { it.id }) { volunteer ->
                    ResponseCard(
                        volunteer = volunteer,
                        isFavorite = volunteer.id in favorites,
                        isSelected = volunteer.id == selectedVolunteerId,
                        onFavoriteToggle = {
                            favorites = if (volunteer.id in favorites) {
                                favorites - volunteer.id
                            } else {
                                favorites + volunteer.id
                            }
                        },
                        onSelect = {
                            selectedVolunteerId =
                                if (selectedVolunteerId == volunteer.id) null else volunteer.id
                        },
                        onClick = { onVolunteerClick(volunteer.id) },
                        modifier = Modifier.animateItem()
                    )
                }
                if (shown.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(
                                if (tab == 0) R.string.text_14_9 else R.string.text_14_6
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponseCard(
    volunteer: Volunteer,
    isFavorite: Boolean,
    isSelected: Boolean,
    onFavoriteToggle: () -> Unit,
    onSelect: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            InitialsAvatar(name = volunteer.name)

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = volunteer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                RatingLabel(rating = volunteer.rating, reviewsCount = volunteer.reviewsCount)
                Text(
                    text = stringResource(R.string.text_13_2, volunteer.experience),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.text_13_3, volunteer.homeShort),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FavoriteButton(isFavorite = isFavorite, onToggle = onFavoriteToggle)
        }

        Spacer(Modifier.height(10.dp))

        SelectButton(isSelected = isSelected, onClick = onSelect)
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "favScale"
    )
    IconButton(onClick = onToggle) {
        Crossfade(targetState = isFavorite, label = "favIcon") { fav ->
            Icon(
                imageVector = if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(if (fav) R.string.text_14_8 else R.string.text_14_7),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
    }
}

@Composable
private fun SelectButton(isSelected: Boolean, onClick: () -> Unit) {
    val container by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
        label = "selectContainer"
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
        label = "selectContent"
    )
    val interaction = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .pressScale(interaction)
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Row {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
        }
        Text(
            text = stringResource(if (isSelected) R.string.text_14_5 else R.string.text_14_4),
            style = MaterialTheme.typography.labelLarge
        )
    }
}