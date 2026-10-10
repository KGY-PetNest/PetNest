package com.example.pet.ui.responses

import com.example.pet.ui.components.ScreenHorizontalPadding
import com.example.pet.ui.components.careFormatText
import com.example.pet.ui.components.icon
import com.example.pet.ui.components.PinnedBarDivider
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.pet.ui.components.PersonThumbnailSize
import com.example.pet.data.displayPersonName
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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.DayMonthFormat
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
import com.example.pet.ui.components.PinnedBarGap
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
import android.content.Intent
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.example.pet.data.phoneForDial
import com.example.pet.ui.components.formatPhone
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import com.example.pet.ui.components.ListThumbnailSize
import com.example.pet.ui.components.showRequestError
import com.example.pet.data.repository.RequestClosedException

@Composable
fun ResponsesScreen(
    requestId: String,
    onBack: () -> Unit,
    onVolunteerClick: (String) -> Unit,
    onEditRequest: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val requests by AppContainer.requests.ownerRequests.collectAsStateWithLifecycle()
    val allReviews by AppContainer.reviews.reviews.collectAsStateWithLifecycle()
    val volunteers by AppContainer.volunteers.volunteers.collectAsStateWithLifecycle()
    val pets by AppContainer.pets.pets.collectAsStateWithLifecycle()
    val responseIds by AppContainer.requests.responses.collectAsStateWithLifecycle()
    val favoriteIds by AppContainer.requests.favorites.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    var completing by remember { mutableStateOf(false) }
    var choosing by remember { mutableStateOf(false) }
    var openingChat by remember { mutableStateOf(false) }
    var showReview by rememberSaveable { mutableStateOf(false) }
    var confirmComplete by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var showLocked by rememberSaveable { mutableStateOf(false) }
    var removed by remember { mutableStateOf(false) }
    var pendingChoice by remember { mutableStateOf<Volunteer?>(null) }
    val responses = remember(volunteers, responseIds, requestId) {
        responseIds[requestId].orEmpty().mapNotNull { id -> volunteers.firstOrNull { it.id == id } }
    }
    val favorites = favoriteIds[requestId].orEmpty()

    val request = requests.firstOrNull { it.id == requestId }
    if (request == null) {
        if (removed) {
            Box(modifier = modifier.fillMaxSize())
        } else {
            NotFoundScreen(title = stringResource(R.string.text_14_1), onBack = onBack, modifier = modifier)
        }
        return
    }
    val petPhotoUri = pets.firstOrNull { it.id == request.petId }?.photoUri ?: request.petPhotoUri
    val chosen = volunteers.firstOrNull { it.id == request.chosenVolunteerId }
    val finishesEarly = LocalDate.now().isBefore(request.end)

    val shown = when {
        request.status == RequestStatus.Completed -> listOfNotNull(chosen)
        tab == 0 -> responses
        else -> responses.filter { it.id in favorites }
    }

    val reviewed = allReviews.any { it.requestId == request.id }

    fun openChat(volunteerId: String) {
        if (openingChat) return
        openingChat = true
        scope.launch {
            AppContainer.chats.openChat(UserRole.Owner, request.id, volunteerId)
                .onSuccess { onOpenChat(it.id) }
                .onFailure { showRequestError(context) }
            openingChat = false
        }
    }

    if (confirmComplete) {
        AlertDialog(
            onDismissRequest = { confirmComplete = false },
            title = { Text(stringResource(if (finishesEarly) R.string.text_15_17 else R.string.text_15_11)) },
            text = {
                Text(
                    if (finishesEarly) {
                        stringResource(
                            R.string.text_15_16,
                            request.end.format(DayMonthFormat),
                            chosen?.let { shortPersonName(it.name) }.orEmpty()
                        )
                    } else {
                        stringResource(R.string.text_15_12)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmComplete = false
                        scope.launch {
                            completing = true
                            AppContainer.requests.complete(request.id)
                                .onSuccess { showReview = true }
                                .onFailure { showRequestError(context) }
                            completing = false
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

    pendingChoice?.let { volunteer ->
        val unselect = request.chosenVolunteerId == volunteer.id
        val replaced = chosen?.takeIf { !unselect }
        AlertDialog(
            onDismissRequest = { pendingChoice = null },
            title = { Text(stringResource(if (unselect) R.string.text_14_13 else R.string.text_14_11)) },
            text = {
                Text(
                    when {
                        unselect && request.start.isBefore(LocalDate.now()) ->
                            stringResource(R.string.text_14_23, shortPersonName(volunteer.name))
                        unselect -> stringResource(R.string.text_14_14, shortPersonName(volunteer.name))
                        replaced != null -> stringResource(
                            R.string.text_14_19,
                            shortPersonName(replaced.name),
                            shortPersonName(volunteer.name)
                        )
                        else -> stringResource(R.string.text_14_12, shortPersonName(volunteer.name))
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingChoice = null
                        if (!choosing) {
                            scope.launch {
                                choosing = true
                                AppContainer.requests.chooseVolunteer(request.id, if (unselect) null else volunteer.id)
                                    .onFailure {
                                        showRequestError(
                                            context,
                                            if (it is RequestClosedException) R.string.text_14_24 else R.string.common_request_error
                                        )
                                    }
                                choosing = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(if (unselect) R.string.text_14_17 else R.string.text_14_4))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingChoice = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showLocked) {
        AlertDialog(
            onDismissRequest = { showLocked = false },
            title = { Text(stringResource(R.string.text_14_21)) },
            text = {
                Text(
                    stringResource(
                        R.string.text_14_22,
                        chosen?.let { shortPersonName(it.name) }.orEmpty()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocked = false
                        pendingChoice = chosen
                    }
                ) {
                    Text(stringResource(R.string.text_14_17))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocked = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.text_14_15)) },
            text = { Text(stringResource(R.string.text_14_16, request.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        scope.launch {
                            AppContainer.requests.delete(request.id).onSuccess {
                                removed = true
                                onBack()
                            }.onFailure { showRequestError(context) }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
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
                .padding(horizontal = ScreenHorizontalPadding)
        ) {
            ScreenHeader(
                title = stringResource(R.string.text_14_1),
                onBack = onBack,
                actions = if (request.status != RequestStatus.Completed) {
                    {
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.text_5_26)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        menuOpen = false
                                        if (request.status == RequestStatus.Open) onEditRequest(request.id) else showLocked = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.text_14_15),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuOpen = false
                                        if (request.status == RequestStatus.Open) confirmDelete = true else showLocked = true
                                    }
                                )
                            }
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
                PetThumbnail(photoUri = petPhotoUri, size = ListThumbnailSize)
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
                    IconLine(icon = request.format.icon, text = careFormatText(request, forOwner = true), maxLines = 1)
                }
            }

            if (request.isExpired) {
                IconLine(
                    icon = Icons.Default.Info,
                    text = stringResource(R.string.text_14_20),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    textColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
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
                state = listState,
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
                        phone = volunteer.phone.takeIf {
                            it.isNotBlank() && volunteer.id == request.chosenVolunteerId &&
                                    request.status != RequestStatus.Open
                        },
                        onCall = { number ->
                            val intent = Intent(Intent.ACTION_DIAL, "tel:${phoneForDial(number)}".toUri())
                            runCatching { context.startActivity(intent) }
                        },
                        selectable = request.status != RequestStatus.Completed &&
                                (volunteer.id == request.chosenVolunteerId || !request.start.isBefore(LocalDate.now())),
                        canFavorite = request.status != RequestStatus.Completed,
                        onFavoriteToggle = { AppContainer.requests.toggleFavorite(request.id, volunteer.id) },
                        onSelect = { pendingChoice = volunteer },
                        onChat = { openChat(volunteer.id) },
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
                enter = fadeIn() + expandVertically(clip = false),
                exit = fadeOut() + shrinkVertically(clip = false)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PinnedBarDivider(visible = listState.canScrollForward)
                    Spacer(Modifier.height(PinnedBarGap))
                    val canComplete = !LocalDate.now().isBefore(request.start)
                    val hint = when {
                        !canComplete -> stringResource(R.string.text_14_18, request.start.format(DayMonthFormat))
                        finishesEarly -> stringResource(R.string.text_14_28, request.end.format(DayMonthFormat))
                        else -> null
                    }
                    if (hint != null) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    PrimaryButton(
                        text = stringResource(
                            if (canComplete && finishesEarly) R.string.text_14_27 else R.string.text_14_10
                        ),
                        loading = completing,
                        enabled = canComplete,
                        onClick = { confirmComplete = true }
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }

            AnimatedVisibility(
                visible = request.status == RequestStatus.Completed,
                enter = fadeIn() + expandVertically(clip = false),
                exit = fadeOut() + shrinkVertically(clip = false)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PinnedBarDivider(visible = listState.canScrollForward)
                    Spacer(Modifier.height(PinnedBarGap))
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
    phone: String?,
    onCall: (String) -> Unit,
    selectable: Boolean,
    canFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onSelect: () -> Unit,
    onChat: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(name = volunteer.name, photoUri = volunteer.avatarUri, size = PersonThumbnailSize)

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = displayPersonName(volunteer.name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingLabel(rating = rating, reviewsCount = reviewsCount)
                    Text(
                        text = stringResource(
                            R.string.text_14_26,
                            if (volunteer.completedCount > 0) {
                                pluralStringResource(R.plurals.completed_count, volunteer.completedCount, volunteer.completedCount)
                            } else {
                                stringResource(R.string.text_14_25)
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            IconButton(onClick = onChat) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = stringResource(R.string.text_10_13),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (canFavorite) {
                FavoriteButton(isFavorite = isFavorite, onToggle = onFavoriteToggle)
            }
        }

        if (phone != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = PersonThumbnailSize + 12.dp, top = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onCall(phone) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = stringResource(R.string.text_23_5),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatPhone(phone),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
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
