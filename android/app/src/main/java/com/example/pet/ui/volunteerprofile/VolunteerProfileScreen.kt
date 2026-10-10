package com.example.pet.ui.volunteerprofile

import com.example.pet.ui.components.ScreenContentInset
import com.example.pet.ui.components.ScreenHorizontalPadding
import java.util.Locale
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pet.ui.components.serviceLabel
import com.example.pet.data.CareFormat
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.HomeConditionGroup
import com.example.pet.data.HomeConditionType
import com.example.pet.data.averageRating
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.ReviewCard
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.TagChip
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.icon
import com.example.pet.ui.components.label
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.remember
import com.example.pet.data.UserRole
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.formatPhone
import com.example.pet.ui.components.ProfileHeader
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.ui.res.pluralStringResource
import com.example.pet.ui.components.StatTile
import com.example.pet.ui.theme.PetStar

private const val REVIEWS_PREVIEW_COUNT = 2
private const val FACT_COLUMNS = 2

private val MonthYearFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("ru"))

@Composable
fun VolunteerProfileScreen(
    volunteerId: String,
    onAllReviews: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null
) {
    val volunteers by AppContainer.volunteers.volunteers.collectAsStateWithLifecycle()
    val allReviews by AppContainer.reviews.reviews.collectAsStateWithLifecycle()
    val ownProfile by remember { AppContainer.profiles.profile(UserRole.Volunteer) }.collectAsStateWithLifecycle()
    val isOwn = onOpenSettings != null
    val primary = MaterialTheme.colorScheme.primary
    val bodyStyle = MaterialTheme.typography.bodyLarge
    val volunteer = volunteers.firstOrNull { it.id == volunteerId }
    if (volunteer == null) {
        NotFoundScreen(title = stringResource(R.string.text_13_1), onBack = onBack, modifier = modifier)
        return
    }
    val reviews = allReviews.filter { it.volunteerId == volunteer.id }

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
                title = stringResource(R.string.text_13_1),
                onBack = onBack,
                actions = if (onOpenSettings != null) {
                    {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.text_16_2),
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
                    .padding(horizontal = ScreenContentInset)
            ) {
                Spacer(Modifier.height(12.dp))

                ProfileHeader(name = volunteer.name, photoUri = volunteer.avatarUri) {
                    IconLine(
                        icon = Icons.Default.CalendarMonth,
                        text = stringResource(R.string.text_13_19, volunteer.joinedAt.format(MonthYearFormat)),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    if (isOwn) {
                        IconLine(
                            icon = Icons.Default.Phone,
                            text = formatPhone(volunteer.phone.ifBlank { ownProfile.phone }),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        IconLine(
                            icon = Icons.Default.Email,
                            text = ownProfile.email,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatTile(
                        value = if (reviews.isEmpty()) {
                            stringResource(R.string.text_13_12)
                        } else {
                            stringResource(R.string.text_15_2, reviews.averageRating())
                        },
                        caption = if (reviews.isEmpty()) {
                            stringResource(R.string.text_13_17)
                        } else {
                            pluralStringResource(R.plurals.reviews_count, reviews.size, reviews.size)
                        },
                        icon = Icons.Default.Star,
                        iconTint = PetStar,
                        onClick = if (reviews.isNotEmpty()) {
                            { onAllReviews(volunteer.id) }
                        } else {
                            null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = volunteer.completedCount.toString(),
                        caption = pluralStringResource(R.plurals.completed_caption, volunteer.completedCount),
                        icon = Icons.Default.TaskAlt,
                        modifier = Modifier.weight(1f)
                    )
                }

                Section(title = stringResource(R.string.text_13_4)) {
                    Text(text = volunteer.about, style = bodyStyle)
                    if (volunteer.experience.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.text_13_16, volunteer.experience),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Section(title = stringResource(R.string.text_13_18)) {
                    FactGrid(
                        items = volunteer.formats.map { format ->
                            format.icon to stringResource(format.serviceLabel)
                        }
                    )
                }

                if (CareFormat.AtVolunteer in volunteer.formats) {
                    Section(title = stringResource(R.string.text_13_5)) {
                        HomeConditionsGrid(volunteer.homeConditions)
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

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HomeConditionsGrid(conditions: List<HomeConditionType>) {
    if (conditions.isEmpty()) {
        Text(
            text = stringResource(R.string.text_13_10),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    val ordered = HomeConditionGroup.entries.flatMap { group -> conditions.filter { it.group == group } }
    FactGrid(items = ordered.map { it.icon to stringResource(it.label) })
}

@Composable
private fun FactGrid(items: List<Pair<ImageVector, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items.chunked(FACT_COLUMNS).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { (icon, text) -> FactItem(icon, text, Modifier.weight(1f)) }
                repeat(FACT_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun FactItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(20.dp))
    SectionTitle(text = title)
    Spacer(Modifier.height(12.dp))
    content()
}
