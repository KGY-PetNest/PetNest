package com.example.pet.ui.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.Pet
import com.example.pet.data.UserRole
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.InitialsAvatar
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.formatPhone
import com.example.pet.ui.components.pressScale

private const val TILES_PER_ROW = 4
private val TILE_AVATAR = 64.dp

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onAddPetClick: () -> Unit,
    onPetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val profile by AppContainer.profiles.profile(UserRole.Owner).collectAsStateWithLifecycle()
    val pets by AppContainer.pets.pets.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.text_9_1),
            onBack = onBack,
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.text_16_2),
                        tint = primary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                InitialsAvatar(name = profile.name, photoUri = profile.avatarUri, size = 80.dp, zoomable = true)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconLine(
                        icon = Icons.Default.Phone,
                        text = formatPhone(profile.phone),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    IconLine(
                        icon = Icons.Default.Email,
                        text = profile.email,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            SectionTitle(
                text = stringResource(R.string.text_9_2),
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(Modifier.height(12.dp))

            val tiles: List<Pet?> = pets + null
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.animateContentSize()
            ) {
                tiles.chunked(TILES_PER_ROW).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { pet ->
                            Box(
                                contentAlignment = Alignment.TopCenter,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (pet != null) {
                                    PetTile(pet = pet, onClick = { onPetClick(pet.id) })
                                } else {
                                    AddPetTile(onClick = onAddPetClick)
                                }
                            }
                        }
                        repeat(TILES_PER_ROW - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PetTile(pet: Pet, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .pressScale(interaction, pressedScale = 0.94f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        PetThumbnail(photoUri = pet.photoUri, size = TILE_AVATAR)
        Spacer(Modifier.height(8.dp))
        Text(
            text = pet.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = pet.info,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AddPetTile(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val primary = MaterialTheme.colorScheme.primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .pressScale(interaction, pressedScale = 0.94f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(TILE_AVATAR)
                .clip(CircleShape)
                .border(1.5.dp, primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.text_9_4),
            style = MaterialTheme.typography.bodyMedium,
            color = primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}