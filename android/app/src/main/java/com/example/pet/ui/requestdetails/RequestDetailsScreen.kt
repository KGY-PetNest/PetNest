package com.example.pet.ui.requestdetails

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.MockData
import com.example.pet.data.MyResponseStatus
import com.example.pet.data.UserRole
import com.example.pet.data.distanceKmTo
import com.example.pet.data.formatDistanceKm
import com.example.pet.data.phoneForDial
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.PetTraitChips
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ReviewCard
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.formatPhone
import com.example.pet.ui.theme.extraColors
import kotlinx.coroutines.launch

@Composable
fun RequestDetailsScreen(
    requestId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val feed by AppContainer.requests.feed.collectAsStateWithLifecycle()
    val responded by AppContainer.requests.respondedIds.collectAsStateWithLifecycle()
    val myLocation by AppContainer.settings.volunteerLocation.collectAsStateWithLifecycle()
    val allReviews by AppContainer.reviews.reviews.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    var openingChat by remember { mutableStateOf(false) }
    val bodyStyle = MaterialTheme.typography.bodyLarge
    val request = feed.firstOrNull { it.id == requestId }
    if (request == null) {
        NotFoundScreen(title = stringResource(R.string.text_21_1), onBack = onBack, modifier = modifier)
        return
    }
    val isResponded = request.id in responded
    val myStatus = request.statusFor(MockData.CURRENT_VOLUNTEER_ID, responded)
    val isMine = myStatus == MyResponseStatus.Chosen || myStatus == MyResponseStatus.Completed
    val canChat = myStatus != null && myStatus != MyResponseStatus.NotChosen
    val distanceKm = myLocation?.point?.let { origin -> request.location?.let { origin.distanceKmTo(it) } }
    val ownerReview = allReviews.firstOrNull {
        it.requestId == request.id && it.volunteerId == MockData.CURRENT_VOLUNTEER_ID
    }

    fun openChat() {
        if (openingChat) return
        scope.launch {
            openingChat = true
            AppContainer.chats.openChat(UserRole.Volunteer, request.id, MockData.CURRENT_VOLUNTEER_ID)
                .onSuccess { onOpenChat(it.id) }
            openingChat = false
        }
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
                title = stringResource(R.string.text_21_1),
                onBack = onBack,
                actions = if (canChat) {
                    {
                        IconButton(onClick = { openChat() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = stringResource(R.string.text_10_14),
                                tint = MaterialTheme.colorScheme.primary
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
                    PetThumbnail(photoUri = request.petPhotoUri, size = 88.dp)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = request.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = request.petInfo,
                            style = bodyStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                when (myStatus) {
                    MyResponseStatus.Chosen -> StatusBanner(
                        icon = Icons.Default.CheckCircle,
                        title = stringResource(R.string.text_21_6),
                        text = stringResource(R.string.text_21_7),
                        containerColor = MaterialTheme.extraColors.successContainer,
                        contentColor = MaterialTheme.extraColors.success
                    )
                    MyResponseStatus.NotChosen -> StatusBanner(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.text_21_9),
                        containerColor = MaterialTheme.colorScheme.outline,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MyResponseStatus.Completed -> StatusBanner(
                        icon = Icons.Default.TaskAlt,
                        title = stringResource(R.string.text_21_10),
                        containerColor = MaterialTheme.extraColors.warningContainer,
                        contentColor = MaterialTheme.extraColors.warning
                    )
                    else -> Unit
                }

                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconLine(
                        icon = Icons.Default.DateRange,
                        text = stringResource(
                            R.string.text_12_8,
                            pluralStringResource(R.plurals.common_days_count, request.days, request.days),
                            request.dates
                        ),
                        iconSize = 22.dp,
                        textStyle = bodyStyle,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    IconLine(
                        icon = Icons.Default.LocationOn,
                        text = when {
                            isMine -> listOf(request.address, request.addressDetails)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                            distanceKm != null -> stringResource(
                                R.string.text_12_35,
                                formatDistanceKm(distanceKm),
                                request.publicPlace
                            )
                            else -> request.publicPlace
                        },
                        iconSize = 22.dp,
                        textStyle = bodyStyle,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    if (request.ownerName.isNotBlank()) {
                        IconLine(
                            icon = Icons.Default.Person,
                            text = stringResource(R.string.text_21_2, request.ownerName),
                            iconSize = 22.dp,
                            textStyle = bodyStyle,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isMine && request.ownerPhone.isNotBlank()) {
                        IconLine(
                            icon = Icons.Default.Phone,
                            text = formatPhone(request.ownerPhone),
                            iconSize = 22.dp,
                            textStyle = bodyStyle,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (myStatus == null || myStatus == MyResponseStatus.Pending) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.text_21_13),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (myStatus == MyResponseStatus.Completed) {
                    SectionDivider()
                    SectionTitle(stringResource(R.string.text_21_11))
                    Spacer(Modifier.height(10.dp))
                    if (ownerReview != null) {
                        ReviewCard(ownerReview)
                    } else {
                        Text(
                            text = stringResource(R.string.text_21_12),
                            style = bodyStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (request.traits.isNotEmpty() || request.features.isNotBlank()) {
                    SectionDivider()
                    SectionTitle(stringResource(R.string.text_4_6))
                    Spacer(Modifier.height(10.dp))
                    PetTraitChips(traits = request.traits, large = true)
                    if (request.features.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(text = request.features, style = bodyStyle)
                    }
                }

                if (request.comment.isNotBlank()) {
                    SectionDivider()
                    SectionTitle(stringResource(R.string.text_5_11))
                    Spacer(Modifier.height(8.dp))
                    Text(text = request.comment, style = bodyStyle)
                }

                Spacer(Modifier.height(24.dp))
            }

            if (myStatus == MyResponseStatus.Chosen && request.ownerPhone.isNotBlank()) {
                PrimaryButton(
                    text = stringResource(R.string.text_21_8),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:${phoneForDial(request.ownerPhone)}".toUri())
                        runCatching { context.startActivity(intent) }
                    }
                )
            }

            if (myStatus == null || myStatus == MyResponseStatus.Pending) AnimatedContent(
                targetState = isResponded,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "respond",
                modifier = Modifier.fillMaxWidth()
            ) { done ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (done) {
                        IconLine(
                            icon = Icons.Default.CheckCircle,
                            text = stringResource(R.string.text_21_4),
                            iconSize = 22.dp,
                            textStyle = bodyStyle,
                            textColor = MaterialTheme.colorScheme.primary
                        )
                        TextButton(
                            enabled = !busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    AppContainer.requests.cancelResponse(request.id)
                                    busy = false
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.text_21_5),
                                color = MaterialTheme.colorScheme.error.copy(alpha = if (busy) 0.38f else 1f)
                            )
                        }
                    } else {
                        PrimaryButton(
                            text = stringResource(R.string.text_21_3),
                            loading = busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    AppContainer.requests.respond(request.id)
                                    busy = false
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun StatusBanner(
    icon: ImageVector,
    title: String,
    containerColor: Color,
    contentColor: Color,
    text: String? = null
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
