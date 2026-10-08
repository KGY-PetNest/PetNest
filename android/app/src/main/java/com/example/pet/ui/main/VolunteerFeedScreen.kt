package com.example.pet.ui.main

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.DayMonthFormat
import com.example.pet.data.MockData
import com.example.pet.data.MyResponseStatus
import com.example.pet.data.PetKind
import com.example.pet.data.PetRequest
import com.example.pet.data.PetTrait
import com.example.pet.data.SavedLocation
import com.example.pet.data.UserRole
import com.example.pet.data.canChat
import com.example.pet.data.distanceKmTo
import com.example.pet.data.formatDistanceKm
import com.example.pet.ui.components.DateRangeDialog
import com.example.pet.ui.components.FilterPill
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.LocationOutcome
import com.example.pet.ui.components.MyResponseStatusChip
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.PetTraitChips
import com.example.pet.ui.components.PetTraitSelector
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.fetchLocationSilently
import com.example.pet.ui.components.openAppSettings
import com.example.pet.ui.components.openLocationSettings
import com.example.pet.ui.components.rememberFutureDateRangePickerState
import com.example.pet.ui.components.rememberLocationRequester
import com.example.pet.ui.components.toUtcMillis
import com.example.pet.ui.components.utcMillisToLocalDate
import java.time.LocalDate
import kotlinx.coroutines.launch

private enum class FeedSort(@param:StringRes val label: Int) {
    Soonest(R.string.text_12_14),
    Closest(R.string.text_12_36),
    Shortest(R.string.text_12_15),
    Longest(R.string.text_12_16)
}

private val KindFilters = listOf(
    PetKind.Cat to R.string.text_12_3,
    PetKind.Dog to R.string.text_12_4,
    PetKind.Other to R.string.text_12_9
)

private val RadiusOptionsKm = listOf(1, 3, 5, 10, 20)
private const val DEFAULT_RADIUS_KM = 5

private enum class LocationProblem { Denied, Blocked, Off, Failed }

private const val LOCATION_REFRESH_MIN_KM = 0.3

private val MyResponsesOrder = listOf(
    MyResponseStatus.Chosen,
    MyResponseStatus.Pending,
    MyResponseStatus.Completed,
    MyResponseStatus.Expired,
    MyResponseStatus.NotChosen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerFeedScreen(
    onRequestClick: (String) -> Unit,
    onPickLocationOnMap: () -> Unit,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val feed by AppContainer.requests.feed.collectAsStateWithLifecycle()
    val responded by AppContainer.requests.respondedIds.collectAsStateWithLifecycle()
    val myLocation by AppContainer.settings.volunteerLocation.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var kinds by rememberSaveable { mutableStateOf(emptySet<PetKind>()) }
    var radiusKm by rememberSaveable { mutableStateOf<Int?>(null) }
    var freeFrom by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var freeTo by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var sort by rememberSaveable { mutableStateOf(FeedSort.Soonest) }
    var mustHave by rememberSaveable { mutableStateOf(emptySet<PetTrait>()) }
    var exclude by rememberSaveable { mutableStateOf(emptySet<PetTrait>()) }

    var sortMenuOpen by remember { mutableStateOf(false) }
    var datesDialogOpen by remember { mutableStateOf(false) }
    var traitsSheetOpen by remember { mutableStateOf(false) }
    var locationSheetOpen by rememberSaveable { mutableStateOf(false) }
    val feedListState = rememberLazyListState()
    val myListState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var openingChat by remember { mutableStateOf(false) }
    val myLocationLabel = stringResource(R.string.text_12_34)
    var locating by remember { mutableStateOf(false) }
    var locationProblem by rememberSaveable { mutableStateOf<LocationProblem?>(null) }
    val locationRequester = rememberLocationRequester { outcome ->
        locating = false
        locationProblem = when (outcome) {
            is LocationOutcome.Found -> {
                AppContainer.settings.setVolunteerLocation(
                    SavedLocation(outcome.point, myLocationLabel, isAuto = true)
                )
                null
            }
            LocationOutcome.PermissionBlocked -> LocationProblem.Blocked
            LocationOutcome.LocationOff -> LocationProblem.Off
            LocationOutcome.PermissionDenied -> LocationProblem.Denied
            LocationOutcome.Failed -> LocationProblem.Failed
        }
    }

    LaunchedEffect(Unit) {
        val saved = AppContainer.settings.volunteerLocation.value
        when {
            saved == null && !AppContainer.settings.locationPrompted -> {
                AppContainer.settings.markLocationPrompted()
                locating = true
                locationRequester.request()
            }
            saved != null && saved.isAuto -> fetchLocationSilently(context) { point ->
                if (point != null && point.distanceKmTo(saved.point) > LOCATION_REFRESH_MIN_KM) {
                    AppContainer.settings.setVolunteerLocation(SavedLocation(point, myLocationLabel, isAuto = true))
                }
            }
        }
    }

    val locationKey = myLocation?.point?.let { "${it.lat},${it.lon}" }
    var knownLocationKey by rememberSaveable { mutableStateOf(locationKey) }

    LaunchedEffect(locationKey) {
        if (locationKey != null && knownLocationKey == null && radiusKm == null) {
            radiusKm = DEFAULT_RADIUS_KM
            sort = FeedSort.Closest
        }
        knownLocationKey = locationKey
    }
    val datesState = rememberFutureDateRangePickerState()

    val origin = myLocation?.point
    val distances = remember(feed, origin) {
        if (origin == null) {
            emptyMap()
        } else {
            feed.mapNotNull { request -> request.location?.let { request.id to origin.distanceKmTo(it) } }.toMap()
        }
    }

    val requests = remember(feed, kinds, radiusKm, distances, freeFrom, freeTo, sort, mustHave, exclude) {
        feed
            .filter { it.acceptsResponses }
            .filter { request ->
                val kindOk = kinds.isEmpty() || request.kind in kinds
                val radius = radiusKm
                val distance = distances[request.id]
                val radiusOk = radius == null || origin == null || (distance != null && distance <= radius)
                val from = freeFrom
                val to = freeTo
                val datesOk = from == null || to == null ||
                        (!request.start.isBefore(from) && !request.end.isAfter(to))
                val traitsOk = request.traits.containsAll(mustHave) &&
                        request.traits.none { it in exclude }
                kindOk && radiusOk && datesOk && traitsOk
            }
            .let { list ->
                when (sort) {
                    FeedSort.Soonest -> list.sortedBy { it.start }
                    FeedSort.Closest -> list.sortedBy { distances[it.id] ?: Double.MAX_VALUE }
                    FeedSort.Shortest -> list.sortedBy { it.days }
                    FeedSort.Longest -> list.sortedByDescending { it.days }
                }
            }
    }

    val myResponses = remember(feed, responded) {
        feed
            .mapNotNull { request ->
                request.statusFor(MockData.CURRENT_VOLUNTEER_ID, responded)?.let { request to it }
            }
            .sortedWith(compareBy({ MyResponsesOrder.indexOf(it.second) }, { it.first.start }))
    }

    fun resetFilters() {
        kinds = emptySet()
        radiusKm = null
        freeFrom = null
        freeTo = null
        datesState.setSelection(null, null)
        mustHave = emptySet()
        exclude = emptySet()
    }

    fun openChat(requestId: String) {
        if (openingChat) return
        openingChat = true
        scope.launch {
            AppContainer.chats.openChat(UserRole.Volunteer, requestId, MockData.CURRENT_VOLUNTEER_ID)
                .onSuccess { onOpenChat(it.id) }
            openingChat = false
        }
    }

    LifecycleResumeEffect(Unit) {
        if (!locationSheetOpen && AppContainer.settings.volunteerLocation.value == null && sort == FeedSort.Closest) {
            sort = FeedSort.Soonest
        }
        onPauseOrDispose { }
    }

    if (locationSheetOpen) {
        LocationFilterSheet(
            location = myLocation,
            radiusKm = radiusKm,
            locating = locating,
            problem = locationProblem,
            onLocate = {
                locating = true
                locationProblem = null
                locationRequester.request()
            },
            onRadiusChange = { radiusKm = it },
            onPickOnMap = {
                locationSheetOpen = false
                onPickLocationOnMap()
            },
            onDismiss = {
                locationSheetOpen = false
                if (myLocation == null && sort == FeedSort.Closest) sort = FeedSort.Soonest
            }
        )
    }

    if (traitsSheetOpen) {
        TraitsFilterSheet(
            mustHave = mustHave,
            exclude = exclude,
            onMustHaveToggle = { trait ->
                mustHave = if (trait in mustHave) mustHave - trait else mustHave + trait
                exclude = exclude - trait
            },
            onExcludeToggle = { trait ->
                exclude = if (trait in exclude) exclude - trait else exclude + trait
                mustHave = mustHave - trait
            },
            onReset = {
                mustHave = emptySet()
                exclude = emptySet()
            },
            onDismiss = { traitsSheetOpen = false }
        )
    }

    if (datesDialogOpen) {
        DateRangeDialog(
            state = datesState,
            title = stringResource(R.string.text_12_13),
            onConfirm = {
                val start = datesState.selectedStartDateMillis
                val end = datesState.selectedEndDateMillis
                if (start != null && end != null) {
                    freeFrom = utcMillisToLocalDate(start)
                    freeTo = utcMillisToLocalDate(end)
                }
                datesDialogOpen = false
            },
            onReset = {
                datesState.setSelection(null, null)
                freeFrom = null
                freeTo = null
                datesDialogOpen = false
            },
            onDismiss = {
                datesState.setSelection(freeFrom?.toUtcMillis(), freeTo?.toUtcMillis())
                datesDialogOpen = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ScreenHeader(title = stringResource(R.string.text_12_1), onBack = onBack)

        Spacer(Modifier.height(8.dp))

        SegmentedToggle(
            options = listOf(
                stringResource(R.string.text_12_23),
                stringResource(R.string.text_12_24)
            ),
            selectedIndex = tab,
            onSelect = { tab = it }
        )

        Spacer(Modifier.height(12.dp))

        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "volunteerTab",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { currentTab ->
            if (currentTab == 0) {
                Column(modifier = Modifier.fillMaxSize()) {
                    FeedFilters(
                        kinds = kinds,
                        onKindsChange = { kinds = it },
                        radiusKm = radiusKm,
                        hasLocation = myLocation != null,
                        onLocationClick = { locationSheetOpen = true },
                        freeFrom = freeFrom,
                        freeTo = freeTo,
                        onDatesClick = { datesDialogOpen = true },
                        traitsCount = mustHave.size + exclude.size,
                        onTraitsClick = { traitsSheetOpen = true },
                        sort = sort,
                        sortMenuOpen = sortMenuOpen,
                        onSortMenuOpenChange = { sortMenuOpen = it },
                        onSortChange = { option ->
                            sort = option
                            if (option == FeedSort.Closest && myLocation == null) locationSheetOpen = true
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyColumn(
                        state = feedListState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(requests, key = { it.id }) { request ->
                            VolunteerRequestCard(
                                request = request,
                                distanceKm = distances[request.id],
                                responded = request.id in responded,
                                onClick = { onRequestClick(request.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                        if (requests.isEmpty()) {
                            item(key = "empty") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp)
                                        .animateItem()
                                ) {
                                    Text(
                                        text = stringResource(R.string.text_12_7),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    TextButton(onClick = { resetFilters() }) {
                                        Text(stringResource(R.string.text_12_17))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = myListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myResponses, key = { it.first.id }) { (request, status) ->
                        VolunteerRequestCard(
                            request = request,
                            distanceKm = distances[request.id],
                            status = status,
                            onClick = { onRequestClick(request.id) },
                            onChat = if (status.canChat) {
                                { openChat(request.id) }
                            } else {
                                null
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                    if (myResponses.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(R.string.text_12_37),
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
            }
        }
    }
}

@Composable
private fun FeedFilters(
    kinds: Set<PetKind>,
    onKindsChange: (Set<PetKind>) -> Unit,
    radiusKm: Int?,
    hasLocation: Boolean,
    onLocationClick: () -> Unit,
    freeFrom: LocalDate?,
    freeTo: LocalDate?,
    onDatesClick: () -> Unit,
    traitsCount: Int,
    onTraitsClick: () -> Unit,
    sort: FeedSort,
    sortMenuOpen: Boolean,
    onSortMenuOpenChange: (Boolean) -> Unit,
    onSortChange: (FeedSort) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        FilterPill(
            text = if (radiusKm != null && hasLocation) {
                stringResource(R.string.text_12_26, radiusKm)
            } else {
                stringResource(R.string.text_12_25)
            },
            selected = radiusKm != null && hasLocation,
            leadingIcon = Icons.Default.NearMe,
            onClick = onLocationClick
        )

        FilterPill(
            text = stringResource(R.string.text_12_2),
            selected = kinds.isEmpty(),
            onClick = { onKindsChange(emptySet()) }
        )
        KindFilters.forEach { (kind, label) ->
            FilterPill(
                text = stringResource(label),
                selected = kind in kinds,
                onClick = {
                    val updated = if (kind in kinds) kinds - kind else kinds + kind
                    onKindsChange(if (updated.size == PetKind.entries.size) emptySet() else updated)
                }
            )
        }

        FilterPill(
            text = if (freeFrom != null && freeTo != null) {
                stringResource(R.string.text_12_18, freeFrom.format(DayMonthFormat), freeTo.format(DayMonthFormat))
            } else {
                stringResource(R.string.text_12_10)
            },
            selected = freeFrom != null && freeTo != null,
            leadingIcon = Icons.Default.DateRange,
            onClick = onDatesClick
        )

        FilterPill(
            text = if (traitsCount > 0) {
                stringResource(R.string.text_12_20, traitsCount)
            } else {
                stringResource(R.string.text_12_19)
            },
            selected = traitsCount > 0,
            leadingIcon = Icons.Default.Tune,
            onClick = onTraitsClick
        )

        Box {
            FilterPill(
                text = stringResource(sort.label),
                selected = sort != FeedSort.Soonest,
                leadingIcon = Icons.AutoMirrored.Filled.Sort,
                onClick = { onSortMenuOpenChange(true) }
            )
            DropdownMenu(
                expanded = sortMenuOpen,
                onDismissRequest = { onSortMenuOpenChange(false) }
            ) {
                FeedSort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(stringResource(option.label)) },
                        leadingIcon = if (option == sort) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else {
                            null
                        },
                        onClick = {
                            onSortChange(option)
                            onSortMenuOpenChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VolunteerRequestCard(
    request: PetRequest,
    distanceKm: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    responded: Boolean = false,
    status: MyResponseStatus? = null,
    onChat: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(12.dp)
    ) {
        PetThumbnail(photoUri = request.petPhotoUri, size = 72.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                if (status != null) {
                    MyResponseStatusChip(status)
                } else if (responded) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.text_21_4),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconLine(
                icon = Icons.Default.DateRange,
                text = stringResource(
                    R.string.text_12_8,
                    pluralStringResource(R.plurals.common_days_count, request.days, request.days),
                    request.dates
                ),
                maxLines = 1
            )
            IconLine(
                icon = Icons.Default.LocationOn,
                text = if (distanceKm != null) {
                    stringResource(R.string.text_12_35, formatDistanceKm(distanceKm), request.publicPlace)
                } else {
                    request.publicPlace
                },
                maxLines = 1
            )
            if (status == null) {
                PetTraitChips(
                    traits = request.traits,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (onChat != null) {
            IconButton(
                onClick = onChat,
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = stringResource(R.string.text_10_14),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationFilterSheet(
    location: SavedLocation?,
    radiusKm: Int?,
    locating: Boolean,
    problem: LocationProblem?,
    onLocate: () -> Unit,
    onRadiusChange: (Int?) -> Unit,
    onPickOnMap: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.text_12_25),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.text_12_28),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            IconLine(
                icon = Icons.Default.LocationOn,
                text = location?.label?.ifBlank { null } ?: stringResource(R.string.text_12_29),
                iconSize = 20.dp,
                textStyle = MaterialTheme.typography.bodyLarge,
                textColor = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onLocate,
                    enabled = !locating,
                    modifier = Modifier.weight(1f)
                ) {
                    if (locating) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = stringResource(if (locating) R.string.text_12_42 else R.string.text_12_30),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2
                    )
                }
                OutlinedButton(
                    onClick = onPickOnMap,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.text_12_31),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2
                    )
                }
            }

            if (problem != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        when (problem) {
                            LocationProblem.Denied -> R.string.text_12_48
                            LocationProblem.Blocked -> R.string.text_12_44
                            LocationProblem.Off -> R.string.text_12_46
                            LocationProblem.Failed -> R.string.text_12_33
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                when (problem) {
                    LocationProblem.Blocked -> TextButton(onClick = { openAppSettings(context) }) {
                        Text(stringResource(R.string.text_12_45))
                    }
                    LocationProblem.Off -> TextButton(onClick = { openLocationSettings(context) }) {
                        Text(stringResource(R.string.text_12_47))
                    }
                    LocationProblem.Denied, LocationProblem.Failed -> Unit
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.text_12_43),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            if (location == null) {
                Text(
                    text = stringResource(R.string.text_12_32),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPill(
                        text = stringResource(R.string.text_12_27),
                        selected = radiusKm == null,
                        onClick = { onRadiusChange(null) }
                    )
                    RadiusOptionsKm.forEach { km ->
                        FilterPill(
                            text = stringResource(R.string.text_12_26, km),
                            selected = radiusKm == km,
                            onClick = { onRadiusChange(km) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(R.string.common_done),
                onClick = { close() },
                height = 48.dp
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TraitsFilterSheet(
    mustHave: Set<PetTrait>,
    exclude: Set<PetTrait>,
    onMustHaveToggle: (PetTrait) -> Unit,
    onExcludeToggle: (PetTrait) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.text_12_19),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.text_12_21),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            PetTraitSelector(selected = mustHave, onToggle = onMustHaveToggle)

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.text_12_22),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            PetTraitSelector(selected = exclude, onToggle = onExcludeToggle)

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onReset) {
                    Text(stringResource(R.string.common_reset))
                }
                PrimaryButton(
                    text = stringResource(R.string.common_done),
                    onClick = { close() },
                    height = 48.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
