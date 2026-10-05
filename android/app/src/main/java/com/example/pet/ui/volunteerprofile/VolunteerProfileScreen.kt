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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.HomeConditionType
import com.example.pet.data.Volunteer
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.RatingLabel
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.TagChip
import com.example.pet.ui.components.adaptiveContentWidth

@Composable
fun VolunteerProfileScreen(
    volunteer: Volunteer,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onWrite: (() -> Unit)? = null
) {
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
            ScreenHeader(title = stringResource(R.string.text_13_1), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = volunteer.name, size = 88.dp)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = volunteer.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        RatingLabel(rating = volunteer.rating, reviewsCount = volunteer.reviewsCount)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TagChip(stringResource(R.string.text_13_2, volunteer.experience))
                            TagChip(stringResource(R.string.text_13_3, volunteer.homeShort))
                        }
                    }
                }

                Section(title = stringResource(R.string.text_13_4)) {
                    Text(
                        text = volunteer.about,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Section(title = stringResource(R.string.text_13_5)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        volunteer.homeConditions.forEach { condition ->
                            IconLine(
                                icon = condition.type.icon(),
                                text = condition.text,
                                iconSize = 20.dp
                            )
                        }
                    }
                }

                Section(title = stringResource(R.string.text_13_6)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        volunteer.acceptedPets.forEach { TagChip(it) }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            if (onWrite != null) {
                PrimaryButton(
                    text = stringResource(R.string.text_13_7),
                    onClick = onWrite
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(24.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(8.dp))
    content()
}

private fun HomeConditionType.icon(): ImageVector = when (this) {
    HomeConditionType.Apartment -> Icons.Default.Apartment
    HomeConditionType.House -> Icons.Default.Home
    HomeConditionType.NoOtherPets, HomeConditionType.HasOtherPets -> Icons.Default.Pets
    HomeConditionType.SomeoneHome -> Icons.Default.Person
}