package com.example.pet.ui.requestdetails

import com.example.pet.ui.components.ScreenContentInset
import com.example.pet.ui.components.ScreenHorizontalPadding
import com.example.pet.data.shortPersonName
import com.example.pet.data.displayPersonName
import com.example.pet.ui.components.careFormatText
import com.example.pet.ui.components.icon
import com.example.pet.ui.components.PinnedBarGap
import com.example.pet.ui.components.PinnedBarDivider
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.pet.data.canChat
import com.example.pet.data.repository.RequestClosedException
import com.example.pet.data.distanceKmTo
import com.example.pet.data.formatDistanceKm
import com.example.pet.data.phoneForDial
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.MyResponseStatusChip
import com.example.pet.ui.components.NotFoundScreen
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.PetTraitChips
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ReviewCard
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.formatPhone
import com.example.pet.ui.theme.extraColors
import kotlinx.coroutines.launch
import com.example.pet.ui.components.ProfileAvatarSize
import com.example.pet.ui.components.showRequestError

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
    var closedError by remember { mutableStateOf(false) }
    var confirmWithdraw by rememberSaveable { mutableStateOf(false) }
    var chatLocked by rememberSaveable { mutableStateOf(false) }
    var confirmCancel by rememberSaveable { mutableStateOf(false) }
    val bodyStyle = MaterialTheme.typography.bodyLarge
    val request = feed.firstOrNull { it.id == requestId }
    if (request == null) {
        NotFoundScreen(title = stringResource(R.string.text_21_1), onBack = onBack, modifier = modifier)
        return
    }
    val isResponded = request.id in responded
    val myStatus = request.statusFor(MockData.CURRENT_VOLUNTEER_ID, responded)
    val isMine = myStatus == MyResponseStatus.Chosen || myStatus == MyResponseStatus.Completed
    val canChat = myStatus.canChat
    val distanceKm = myLocation?.point?.let { origin -> request.location?.let { origin.distanceKmTo(it) } }
    val ownerReview = allReviews.firstOrNull {
        it.requestId == request.id && it.volunteerId == MockData.CURRENT_VOLUNTEER_ID
    }

    if (confirmWithdraw) {
        AlertDialog(
            onDismissRequest = { confirmWithdraw = false },
            title = { Text(stringResource(R.string.text_21_16)) },
            text = { Text(stringResource(R.string.text_21_17)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmWithdraw = false
                        scope.launch {
                            busy = true
                            AppContainer.requests.withdraw(request.id)
                                .onFailure { showRequestError(context) }
                            busy = false
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.text_21_18),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmWithdraw = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(stringResource(R.string.text_21_22)) },
            text = { Text(stringResource(R.string.text_21_23)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmCancel = false
                        scope.launch {
                            busy = true
                            AppContainer.requests.cancelResponse(request.id)
                                .onFailure { showRequestError(context) }
                            busy = false
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.text_21_5),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (chatLocked) {
        AlertDialog(
            onDismissRequest = { chatLocked = false },
            title = { Text(stringResource(R.string.text_21_20)) },
            text = { Text(stringResource(R.string.text_21_21)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatLocked = false
                        scope.launch {
                            busy = true
                            closedError = false
                            AppContainer.requests.respond(request.id)
                                .onFailure { closedError = it is RequestClosedException }
                            busy = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.text_21_3))
                }
            },
            dismissButton = {
                TextButton(onClick = { chatLocked = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    fun openChat() {
        if (openingChat) return
        openingChat = true
        scope.launch {
            AppContainer.chats.openChat(UserRole.Volunteer, request.id, MockData.CURRENT_VOLUNTEER_ID)
                .onSuccess { onOpenChat(it.id) }
                .onFailure { showRequestError(context) }
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
                .padding(horizontal = ScreenHorizontalPadding)
        ) {
            ScreenHeader(
                title = stringResource(R.string.text_21_1),
                onBack = onBack,
                actions = if (canChat || (myStatus == null && request.acceptsResponses)) {
                    {
                        IconButton(onClick = { if (canChat) openChat() else chatLocked = true }) {
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

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = ScreenContentInset)
            ) {
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PetThumbnail(photoUri = request.petPhotoUri, size = ProfileAvatarSize, zoomTitle = request.title, zoomable = true)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = request.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (request.petInfo.isNotBlank() && request.petInfo != request.title) {
                            Text(
                                text = request.petInfo,
                                style = bodyStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (myStatus == MyResponseStatus.Pending) {
                            MyResponseStatusChip(myStatus)
                        }
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
                    MyResponseStatus.Expired -> StatusBanner(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.text_21_19),
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

                Spacer(Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardSurface()
                        .padding(16.dp)
                ) {
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
                        icon = request.format.icon,
                        text = careFormatText(request, forOwner = false),
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
                            text = stringResource(
                                R.string.text_21_2,
                                if (isMine) displayPersonName(request.ownerName) else shortPersonName(request.ownerName)
                            ),
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
                    if (myStatus == null || myStatus == MyResponseStatus.Pending) {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.text_21_13),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
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

            val hasBottomBar = myStatus == null || myStatus == MyResponseStatus.Pending ||
                    myStatus == MyResponseStatus.Chosen
            if (hasBottomBar) {
                PinnedBarDivider(visible = scrollState.canScrollForward)
                Spacer(Modifier.height(PinnedBarGap))
            }

            if (myStatus == MyResponseStatus.Chosen) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (request.ownerPhone.isNotBlank()) {
                        PrimaryButton(
                            text = stringResource(R.string.text_21_8),
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, "tel:${phoneForDial(request.ownerPhone)}".toUri())
                                runCatching { context.startActivity(intent) }
                            }
                        )
                    }
                    TextButton(
                        enabled = !busy,
                        onClick = { confirmWithdraw = true }
                    ) {
                        Text(
                            text = stringResource(R.string.text_21_15),
                            color = MaterialTheme.colorScheme.error.copy(alpha = if (busy) 0.38f else 1f)
                        )
                    }
                }
            }

            if (myStatus == null && !request.acceptsResponses) {
                IconLine(
                    icon = Icons.Default.Info,
                    text = stringResource(R.string.text_21_14),
                    iconSize = 22.dp,
                    textStyle = bodyStyle,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (myStatus == null || myStatus == MyResponseStatus.Pending) AnimatedContent(
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
                            textColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            enabled = !busy,
                            onClick = { confirmCancel = true }
                        ) {
                            Text(
                                text = stringResource(R.string.text_21_5),
                                color = MaterialTheme.colorScheme.error.copy(alpha = if (busy) 0.38f else 1f)
                            )
                        }
                    } else {
                        if (closedError) {
                            Text(
                                text = stringResource(R.string.text_21_14),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        PrimaryButton(
                            text = stringResource(R.string.text_21_3),
                            loading = busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    closedError = false
                                    AppContainer.requests.respond(request.id)
                                        .onFailure { closedError = it is RequestClosedException }
                                    busy = false
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(if (hasBottomBar) 16.dp else 32.dp))
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
