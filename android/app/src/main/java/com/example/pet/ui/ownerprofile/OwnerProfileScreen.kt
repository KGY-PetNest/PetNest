package com.example.pet.ui.ownerprofile

import com.example.pet.ui.components.ReviewReportSheet
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.shortPersonName
import com.example.pet.ui.components.JoinedSinceLine
import com.example.pet.ui.components.ListItemSpacing
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.ProfileHeader
import com.example.pet.ui.components.ReviewCard
import com.example.pet.ui.components.ScreenBottomGap
import com.example.pet.ui.components.ScreenContentInset
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.ScreenHorizontalPadding
import com.example.pet.ui.components.ScreenSection
import com.example.pet.ui.components.ScreenTopGap
import com.example.pet.ui.components.StatTile
import com.example.pet.ui.components.adaptiveContentWidth

@Composable
fun OwnerProfileScreen(
    ownerId: String,
    onBack: () -> Unit,
    onOpenVolunteer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val owners by AppContainer.owners.owners.collectAsStateWithLifecycle()
    val allReviews by AppContainer.reviews.reviews.collectAsStateWithLifecycle()
    val volunteers by AppContainer.volunteers.volunteers.collectAsStateWithLifecycle()
    var reportReviewId by rememberSaveable { mutableStateOf<String?>(null) }
    val owner = owners.firstOrNull { it.id == ownerId }
    if (owner == null) {
        NotFoundScreen(title = stringResource(R.string.text_28_1), onBack = onBack, modifier = modifier)
        return
    }
    val reviews = allReviews.filter { it.authorId == owner.id }.sortedByDescending { it.date }
    val isOwn = owner.id == AppContainer.owners.currentOwnerId

    reportReviewId?.let { id -> ReviewReportSheet(reviewId = id, onDismiss = { reportReviewId = null }) }

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
            ScreenHeader(title = stringResource(R.string.text_28_1), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenContentInset)
            ) {
                Spacer(Modifier.height(ScreenTopGap))

                ProfileHeader(name = owner.name, photoUri = owner.avatarUri) {
                    JoinedSinceLine(owner.joinedAt)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatTile(
                        value = owner.completedCount.toString(),
                        caption = pluralStringResource(R.plurals.completed_caption, owner.completedCount),
                        icon = Icons.Default.TaskAlt,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = reviews.size.toString(),
                        caption = pluralStringResource(R.plurals.reviews_caption, reviews.size),
                        icon = Icons.Default.RateReview,
                        modifier = Modifier.weight(1f)
                    )
                }

                ScreenSection(title = stringResource(R.string.text_28_2)) {
                    if (reviews.isEmpty()) {
                        Text(
                            text = stringResource(R.string.text_28_3),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(ListItemSpacing)) {
                            reviews.forEach { review ->
                                val volunteer = volunteers.firstOrNull { it.id == review.volunteerId }
                                ReviewCard(
                                    review = review,
                                    personName = volunteer?.let { shortPersonName(it.name) } ?: review.authorName,
                                    personPhotoUri = volunteer?.avatarUri,
                                    onPersonClick = volunteer?.let { { onOpenVolunteer(it.id) } },
                                    onReport = if (isOwn) null else {
                                        { reportReviewId = review.id }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(ScreenBottomGap))
            }
        }
    }
}
