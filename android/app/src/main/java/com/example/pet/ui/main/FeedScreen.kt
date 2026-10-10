package com.example.pet.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.PetRequest
import com.example.pet.data.RequestStatus
import com.example.pet.data.Review
import com.example.pet.data.UserRole
import com.example.pet.data.shortPersonName
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.ReviewSheet
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.TagChip
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.label
import com.example.pet.ui.theme.extraColors
import java.time.LocalDate
import java.util.UUID
import com.example.pet.ui.components.ListThumbnailSize

private const val FAB_COLLAPSE_SCROLL_PX = 48
private val FAB_CLEARANCE = 88.dp

private fun PetRequest.feedRank(): Int = when {
    status == RequestStatus.VolunteerChosen -> 0
    status == RequestStatus.Open && !isExpired -> 1
    status == RequestStatus.Open -> 2
    else -> 3
}

private val OwnerRequestOrder = compareBy<PetRequest>(
    { it.feedRank() },
    { if (it.status == RequestStatus.Completed) -it.start.toEpochDay() else it.start.toEpochDay() }
)

@Composable
fun FeedScreen(
    onCreateClick: () -> Unit,
    onRequestClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val requests by AppContainer.requests.ownerRequests.collectAsStateWithLifecycle()
    val reviews by AppContainer.reviews.reviews.collectAsStateWithLifecycle()
    val volunteers by AppContainer.volunteers.volunteers.collectAsStateWithLifecycle()
    val pets by AppContainer.pets.pets.collectAsStateWithLifecycle()
    val responses by AppContainer.requests.responses.collectAsStateWithLifecycle()
    val reviewedRequestIds = reviews.mapNotNull { it.requestId }.toSet()
    val sortedRequests = remember(requests) { requests.sortedWith(OwnerRequestOrder) }

    var reviewRequestId by rememberSaveable { mutableStateOf<String?>(null) }
    val reviewRequest = requests.firstOrNull { it.id == reviewRequestId }
    val reviewVolunteer = volunteers.firstOrNull { it.id == reviewRequest?.chosenVolunteerId }

    if (reviewRequest != null && reviewVolunteer != null) {
        ReviewSheet(
            volunteerName = reviewVolunteer.name,
            onSubmit = { rating, text ->
                AppContainer.reviews.add(
                    Review(
                        id = UUID.randomUUID().toString(),
                        volunteerId = reviewVolunteer.id,
                        requestId = reviewRequest.id,
                        authorName = shortPersonName(AppContainer.profiles.profile(UserRole.Owner).value.name),
                        rating = rating,
                        text = text,
                        date = LocalDate.now()
                    )
                )
            },
            onDismiss = { reviewRequestId = null }
        )
    }

    val scrollState = rememberScrollState()
    val fabExpanded by remember { derivedStateOf { scrollState.value < FAB_COLLAPSE_SCROLL_PX } }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(
                title = stringResource(R.string.text_8_1),
                onBack = onBack
            )

            if (requests.isEmpty()) {
                EmptyFeed(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sortedRequests.forEach { request ->
                            RequestCard(
                                request = request,
                                photoUri = pets.firstOrNull { it.id == request.petId }?.photoUri ?: request.petPhotoUri,
                                responsesCount = responses[request.id].orEmpty().size,
                                volunteerName = volunteers.firstOrNull { it.id == request.chosenVolunteerId }?.name,
                                reviewed = request.id in reviewedRequestIds,
                                onClick = { onRequestClick(request.id) },
                                onLeaveReview = { reviewRequestId = request.id }
                            )
                        }
                    }

                    Spacer(Modifier.height(FAB_CLEARANCE))
                }
            }
        }

        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = stringResource(R.string.text_8_2),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            expanded = fabExpanded,
            onClick = onCreateClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun EmptyFeed(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.text_8_4),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.text_8_5),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(FAB_CLEARANCE))
    }
}

@Composable
private fun RequestCard(
    request: PetRequest,
    photoUri: String?,
    responsesCount: Int,
    volunteerName: String?,
    reviewed: Boolean,
    onClick: () -> Unit,
    onLeaveReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            PetThumbnail(photoUri = photoUri, size = ListThumbnailSize)

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
                    if (request.isExpired) {
                        TagChip(
                            text = stringResource(R.string.text_8_7),
                            containerColor = MaterialTheme.colorScheme.outline,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        StatusChip(request.status)
                    }
                }
                IconLine(icon = Icons.Default.DateRange, text = request.dates, maxLines = 1)
                IconLine(icon = Icons.Default.LocationOn, text = request.place, maxLines = 1)
                if (volunteerName != null && request.status != RequestStatus.Open) {
                    IconLine(
                        icon = Icons.Default.Person,
                        text = stringResource(R.string.text_8_6, shortPersonName(volunteerName)),
                        maxLines = 1
                    )
                }
                if (request.isExpired) {
                    IconLine(
                        icon = Icons.Default.Info,
                        text = stringResource(R.string.text_8_8),
                        textColor = MaterialTheme.colorScheme.error
                    )
                } else if (request.status == RequestStatus.VolunteerChosen && !LocalDate.now().isBefore(request.end)) {
                    IconLine(
                        icon = Icons.Default.Info,
                        text = stringResource(R.string.text_8_9),
                        textColor = MaterialTheme.colorScheme.primary
                    )
                } else if (request.status == RequestStatus.Open) {
                    IconLine(
                        icon = Icons.Default.Groups,
                        text = if (responsesCount > 0) {
                            pluralStringResource(R.plurals.responses_count, responsesCount, responsesCount)
                        } else {
                            stringResource(R.string.text_8_3)
                        },
                        textColor = if (responsesCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1
                    )
                }
            }
        }

        if (request.status == RequestStatus.Completed) {
            Spacer(Modifier.height(10.dp))
            if (reviewed) {
                IconLine(
                    icon = Icons.Default.CheckCircle,
                    text = stringResource(R.string.text_15_10),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            } else {
                Button(
                    onClick = onLeaveReview,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.text_15_4), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: RequestStatus) {
    val (container, content) = when (status) {
        RequestStatus.Open -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        RequestStatus.VolunteerChosen -> MaterialTheme.extraColors.warningContainer to MaterialTheme.extraColors.warning
        RequestStatus.Completed -> MaterialTheme.extraColors.successContainer to MaterialTheme.extraColors.success
    }
    TagChip(
        text = stringResource(status.label),
        containerColor = container,
        contentColor = content
    )
}
