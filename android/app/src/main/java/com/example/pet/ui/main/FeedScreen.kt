package com.example.pet.ui.main

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.PetRequest
import com.example.pet.data.RequestStatus
import com.example.pet.data.Review
import com.example.pet.data.UserRole
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ReviewSheet
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.TagChip
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.label
import com.example.pet.ui.theme.extraColors
import java.time.LocalDate
import java.util.UUID

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
    val reviewedRequestIds = reviews.mapNotNull { it.requestId }.toSet()

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
                        authorName = AppContainer.profiles.profile(UserRole.Owner).value.name,
                        rating = rating,
                        text = text,
                        date = LocalDate.now()
                    )
                )
            },
            onDismiss = { reviewRequestId = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = stringResource(R.string.text_8_1),
            onBack = onBack
        )

        Spacer(Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            requests.forEach { request ->
                RequestCard(
                    request = request,
                    responsesCount = AppContainer.volunteers.responsesFor(request.id).size,
                    reviewed = request.id in reviewedRequestIds,
                    onClick = { onRequestClick(request.id) },
                    onLeaveReview = { reviewRequestId = request.id }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.text_8_2),
            onClick = onCreateClick
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RequestCard(
    request: PetRequest,
    responsesCount: Int,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetThumbnail(size = 56.dp)

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = request.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                IconLine(icon = Icons.Default.DateRange, text = request.dates)
                IconLine(icon = Icons.Default.LocationOn, text = request.place)
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(request.status)
                if (request.status == RequestStatus.Open) {
                    TagChip(
                        text = if (responsesCount > 0) {
                            pluralStringResource(R.plurals.responses_count, responsesCount, responsesCount)
                        } else {
                            stringResource(R.string.text_8_3)
                        }
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