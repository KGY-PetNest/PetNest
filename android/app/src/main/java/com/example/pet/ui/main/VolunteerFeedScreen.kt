package com.example.pet.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.AppContainer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.CheckCircle
import com.example.pet.data.DayMonthFormat
import com.example.pet.data.MockData
import com.example.pet.data.PetKind
import com.example.pet.data.PetRequest
import com.example.pet.ui.components.FilterPill
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.PetTraitChips
import com.example.pet.ui.components.DateRangeDialog
import com.example.pet.ui.components.cardSurface
import com.example.pet.ui.components.rememberFutureDateRangePickerState
import com.example.pet.ui.components.toUtcMillis
import com.example.pet.ui.components.utcMillisToLocalDate
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.pet.data.PetTrait
import com.example.pet.ui.components.PetTraitSelector
import com.example.pet.ui.components.PrimaryButton
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class FeedSort(@param:StringRes val label: Int) {
    Nearest(R.string.text_12_14),
    Shortest(R.string.text_12_15),
    Longest(R.string.text_12_16)
}

private val KindFilters = listOf(
    PetKind.Cat to R.string.text_12_3,
    PetKind.Dog to R.string.text_12_4,
    PetKind.Other to R.string.text_12_9
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerFeedScreen(
    onRequestClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val feed by AppContainer.requests.feed.collectAsStateWithLifecycle()
    val responded by AppContainer.requests.respondedIds.collectAsStateWithLifecycle()
    var kinds by rememberSaveable { mutableStateOf(emptySet<PetKind>()) }
    var district by rememberSaveable { mutableStateOf<String?>(null) }
    var freeFrom by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var freeTo by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var sort by rememberSaveable { mutableStateOf(FeedSort.Nearest) }

    var districtMenuOpen by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var datesDialogOpen by remember { mutableStateOf(false) }
    var traitsSheetOpen by remember { mutableStateOf(false) }
    var mustHave by rememberSaveable { mutableStateOf(emptySet<PetTrait>()) }
    var exclude by rememberSaveable { mutableStateOf(emptySet<PetTrait>()) }
    val datesState = rememberFutureDateRangePickerState()

    val requests = remember(feed, kinds, district, freeFrom, freeTo, sort, mustHave, exclude) {
        feed
            .filter { request ->
                val kindOk = kinds.isEmpty() || request.kind in kinds
                val districtOk = district == null || request.district == district
                val from = freeFrom
                val to = freeTo
                val datesOk = from == null || to == null ||
                        (!request.start.isBefore(from) && !request.end.isAfter(to))
                val traitsOk = request.traits.containsAll(mustHave) &&
                        request.traits.none { it in exclude }
                kindOk && districtOk && datesOk && traitsOk
            }
            .let { list ->
                when (sort) {
                    FeedSort.Nearest -> list.sortedBy { it.start }
                    FeedSort.Shortest -> list.sortedBy { it.days }
                    FeedSort.Longest -> list.sortedByDescending { it.days }
                }
            }
    }

    fun resetFilters() {
        kinds = emptySet()
        district = null
        freeFrom = null
        freeTo = null
        datesState.setSelection(null, null)
        mustHave = emptySet()
        exclude = emptySet()
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            FilterPill(
                text = stringResource(R.string.text_12_2),
                selected = kinds.isEmpty(),
                onClick = { kinds = emptySet() }
            )
            KindFilters.forEach { (kind, label) ->
                FilterPill(
                    text = stringResource(label),
                    selected = kind in kinds,
                    onClick = {
                        val updated = if (kind in kinds) kinds - kind else kinds + kind
                        kinds = if (updated.size == PetKind.entries.size) emptySet() else updated
                    }
                )
            }

            val from = freeFrom
            val to = freeTo
            FilterPill(
                text = if (from != null && to != null) {
                    stringResource(R.string.text_12_18, from.format(DayMonthFormat), to.format(DayMonthFormat))
                } else {
                    stringResource(R.string.text_12_10)
                },
                selected = from != null && to != null,
                leadingIcon = Icons.Default.DateRange,
                onClick = { datesDialogOpen = true }
            )

            val traitsCount = mustHave.size + exclude.size
            FilterPill(
                text = if (traitsCount > 0) {
                    stringResource(R.string.text_12_20, traitsCount)
                } else {
                    stringResource(R.string.text_12_19)
                },
                selected = traitsCount > 0,
                leadingIcon = Icons.Default.Tune,
                onClick = { traitsSheetOpen = true }
            )

            Box {
                FilterPill(
                    text = district ?: stringResource(R.string.text_12_5),
                    selected = district != null,
                    leadingIcon = Icons.Default.LocationOn,
                    onClick = { districtMenuOpen = true }
                )
                DropdownMenu(
                    expanded = districtMenuOpen,
                    onDismissRequest = { districtMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.text_12_6)) },
                        onClick = {
                            district = null
                            districtMenuOpen = false
                        }
                    )
                    MockData.districts.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                district = item
                                districtMenuOpen = false
                            }
                        )
                    }
                }
            }

            Box {
                FilterPill(
                    text = stringResource(sort.label),
                    selected = sort != FeedSort.Nearest,
                    leadingIcon = Icons.AutoMirrored.Filled.Sort,
                    onClick = { sortMenuOpen = true }
                )
                DropdownMenu(
                    expanded = sortMenuOpen,
                    onDismissRequest = { sortMenuOpen = false }
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
                                sort = option
                                sortMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(requests, key = { it.id }) { request ->
                VolunteerRequestCard(
                    request = request,
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
}

@Composable
private fun VolunteerRequestCard(
    request: PetRequest,
    responded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(12.dp)
    ) {
        PetThumbnail(size = 72.dp)

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
                    modifier = Modifier.weight(1f)
                )
                if (responded) {
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
                )
            )
            IconLine(icon = Icons.Default.LocationOn, text = request.place)
            PetTraitChips(
                traits = request.traits,
                modifier = Modifier.padding(top = 4.dp)
            )
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