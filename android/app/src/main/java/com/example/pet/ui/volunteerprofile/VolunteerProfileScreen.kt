package com.example.pet.ui.volunteerprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.averageRating
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.RatingLabel
import com.example.pet.ui.components.ReviewCard
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.SettingsRow
import com.example.pet.ui.components.TagChip
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.icon
import com.example.pet.ui.components.label

private const val REVIEWS_PREVIEW_COUNT = 2

@Composable
fun VolunteerProfileScreen(
    volunteerId: String,
    onAllReviews: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onEditProfile: (() -> Unit)? = null,
    onChangePassword: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    val volunteers by AppContainer.volunteers.volunteers.collectAsStateWithLifecycle()
    val allReviews by AppContainer.reviews.reviews.collectAsStateWithLifecycle()
    val volunteer = volunteers.firstOrNull { it.id == volunteerId } ?: volunteers.first()
    val reviews = allReviews.filter { it.volunteerId == volunteer.id }
    val primary = MaterialTheme.colorScheme.primary
    val bodyStyle = MaterialTheme.typography.bodyLarge

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
                title = stringResource(R.string.text_13_1),
                onBack = onBack,
                actions = if (onEditProfile != null) {
                    {
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.text_16_1),
                                tint = primary
                            )
                        }
                    }
                } else {
                    null
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = volunteer.name, size = 96.dp)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = volunteer.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        RatingLabel(
                            rating = reviews.averageRating(),
                            reviewsCount = reviews.size,
                            large = true
                        )
                        TagChip(
                            text = stringResource(R.string.text_13_2, volunteer.experience),
                            large = true
                        )
                    }
                }

                Section(title = stringResource(R.string.text_13_4)) {
                    Text(text = volunteer.about, style = bodyStyle)
                }

                Section(title = stringResource(R.string.text_13_5)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        volunteer.homeConditions.forEach { condition ->
                            IconLine(
                                icon = condition.icon,
                                text = stringResource(condition.label),
                                iconSize = 24.dp,
                                textStyle = bodyStyle,
                                textColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Section(title = stringResource(R.string.text_13_6)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        volunteer.acceptedPets.forEach { TagChip(stringResource(it.label), large = true) }
                    }
                }

                Section(title = stringResource(R.string.text_15_1)) {
                    if (reviews.isEmpty()) {
                        Text(
                            text = stringResource(R.string.text_15_8),
                            style = bodyStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            reviews.take(REVIEWS_PREVIEW_COUNT).forEach { ReviewCard(it) }
                        }
                        if (reviews.size > REVIEWS_PREVIEW_COUNT) {
                            TextButton(onClick = { onAllReviews(volunteer.id) }) {
                                Text(
                                    text = stringResource(R.string.text_15_9, reviews.size),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                if (onChangePassword != null || onLogout != null) {
                    Section(title = stringResource(R.string.text_16_2)) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (onEditProfile != null) {
                                SettingsRow(
                                    icon = Icons.Default.Edit,
                                    text = stringResource(R.string.text_16_1),
                                    onClick = onEditProfile
                                )
                            }
                            if (onChangePassword != null) {
                                SettingsRow(
                                    icon = Icons.Default.Lock,
                                    text = stringResource(R.string.text_16_3),
                                    onClick = onChangePassword
                                )
                            }
                            if (onLogout != null) {
                                SettingsRow(
                                    icon = Icons.AutoMirrored.Filled.Logout,
                                    text = stringResource(R.string.text_16_4),
                                    onClick = onLogout,
                                    danger = true
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(28.dp))
    SectionTitle(text = title)
    Spacer(Modifier.height(12.dp))
    content()
}