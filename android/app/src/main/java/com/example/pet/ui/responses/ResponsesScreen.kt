package com.example.pet.ui.responses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.RequestStatus
import com.example.pet.data.Review
import com.example.pet.data.UserRole
import com.example.pet.data.Volunteer
import com.example.pet.data.averageRating
import com.example.pet.data.shortPersonName
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.RatingLabel
import com.example.pet.ui.components.ReviewSheet
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.pressScale
import com.example.pet.ui.main.StatusChip
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun ResponsesScreen(
    requestId: String,
    onBack: () -> Unit,
    onVolunteerClick: (String) -> Unit,
    onEditRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val requests by AppContainer.requests.ownerRequests.collectAsStateWithLifecycle()
    val allReviews by AppContainer.reviews.reviews.collectAsStateWithLifecycle()
    val volunteers by AppContainer.volunteers.volunteers.collectAsStateWithLifecycle()
    val pets by AppContainer.pets.pets.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var favorites by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var completing by remember { mutableStateOf(false) }
    var showReview by rememberSaveable { mutableStateOf(false) }
    var confirmComplete by rememberSaveable { mutableStateOf(false) }
    val responses = remember(volunteers, requestId) { AppContainer.volunteers.responsesFor(requestId) }

    val request = requests.firstOrNull { it.id == requestId }
    if (request == null) {
        NotFoundScreen(title = stringResource(R.string.text_14_1), onBack = onBack, modifier = modifier)
        return
    }
    val petPhotoUri = pets.firstOrNull { it.id == request.petId }?.photoUri ?: request.petPhotoUri
    val chosen = responses.firstOrNull { it.id == request.chosenVolunteerId }

    val shown = when {
        request.status == RequestStatus.Completed -> listOfNotNull(chosen)
        tab == 0 -> responses
        else -> responses.filter { it.id in favorites }
    }

    val reviewed = allReviews.any { it.requestId == request.id }

    if (confirmComplete) {
        AlertDialog(
            onDismissRequest = { confirmComplete = false },
            title = { Text(stringResource(R.string.text_15_11)) },
            text = { Text(stringResource(R.string.text_15_12)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmComplete = false
                        scope.launch {
                            completing = true
                            AppContainer.requests.complete(request.id)
                            completing = false
                            showReview = true
                        }
                    }
                ) {
                    Text(stringResource(R.string.text_15_13))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmComplete = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showReview && chosen != null) {
        ReviewSheet(
            volunteerName = chosen.name,
            onSubmit = { rating, text ->
                AppContainer.reviews.add(
                    Review(
                        id = UUID.randomUUID().toString(),
                        volunteerId = chosen.id,
                        requestId = request.id,
                        authorName = shortPersonName(AppContainer.profiles.profile(UserRole.Owner).value.name),
                        rating = rating,
                        text = text,
                        date = LocalDate.now()
                    )
                )
            },
            onDismiss = { showReview = false }
        )
    }

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
            ScreenHeader(
                title = stringResource(R.string.text_14_1),
                onBack = onBack,
                actions = if (request.status == RequestStatus.Open) {
                    {
                        IconButton(onClick = { onEditRequest(request.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.text_5_26),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    null
                }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .cardSurface()
                    .padding(12.dp)
            ) {
                PetThumbnail(photoUri = petPhotoUri, size = 56.dp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = request.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        StatusChip(request.status)
                    }
                    Text(
                        text = request.petInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconLine(icon = Icons.Default.DateRange, text = request.dates, maxLines = 1)
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = request.status != RequestStatus.Completed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    SegmentedToggle(
                        options = listOf(
                            stringResource(R.string.text_14_2),
                            stringResource(R.string.text_14_3)
                        ),
                        selectedIndex = tab,
                        onSelect = { tab = it }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

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
                        rating = allReviews.filter { it.volunteerId == volunteer.id }.averageRating(),
                        reviewsCount = allReviews.count { it.volunteerId == volunteer.id },
                        isFavorite = volunteer.id in favorites,
                        isSelected = volunteer.id == request.chosenVolunteerId,
                        selectable = request.status != RequestStatus.Completed,
                        onFavoriteToggle = {
                            favorites = if (volunteer.id in favorites) favorites - volunteer.id else favorites + volunteer.id
                        },
                        onSelect = {
                            scope.launch {
                                AppContainer.requests.chooseVolunteer(
                                    request.id,
                                    if (request.chosenVolunteerId == volunteer.id) null else volunteer.id
                                )
                            }
                        },
                        onClick = { onVolunteerClick(volunteer.id) },
                        modifier = Modifier.animateItem()
                    )
                }
                if (shown.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(if (tab == 0) R.string.text_14_9 else R.string.text_14_6),
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

            AnimatedVisibility(
                visible = request.status == RequestStatus.VolunteerChosen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    PrimaryButton(
                        text = stringResource(R.string.text_14_10),
                        loading = completing,
                        onClick = { confirmComplete = true }
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }

            AnimatedVisibility(
                visible = request.status == RequestStatus.Completed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (reviewed) {
                        IconLine(
                            icon = Icons.Default.CheckCircle,
                            text = stringResource(R.string.text_15_10),
                            iconSize = 20.dp,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    } else if (chosen != null) {
                        PrimaryButton(
                            text = stringResource(R.string.text_15_4),
                            onClick = { showReview = true }
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ResponseCard(
    volunteer: Volunteer,
    rating: Double,
    reviewsCount: Int,
    isFavorite: Boolean,
    isSelected: Boolean,
    selectable: Boolean,
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
            InitialsAvatar(name = volunteer.name, photoUri = volunteer.avatarUri, size = 56.dp)

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = volunteer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                RatingLabel(rating = rating, reviewsCount = reviewsCount)
                Text(
                    text = stringResource(R.string.text_13_2, volunteer.experience),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectable) {
                FavoriteButton(isFavorite = isFavorite, onToggle = onFavoriteToggle)
            }
        }

        if (selectable) {
            Spacer(Modifier.height(10.dp))
            SelectButton(isSelected = isSelected, onClick = onSelect)
        }
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
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
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
            .height(42.dp)
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