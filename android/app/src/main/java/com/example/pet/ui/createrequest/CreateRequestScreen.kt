package com.example.pet.ui.createrequest

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onBack: () -> Unit,
    onSelectPet: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val todayStartUtc = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val currentYear = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)
    }

    val pickerState = rememberDateRangePickerState(
        initialDisplayMode = DisplayMode.Picker,
        selectableDates = remember(todayStartUtc, currentYear) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayStartUtc
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year >= currentYear
                }
            }
        }
    )

    var comment by rememberSaveable { mutableStateOf("") }

    val start = pickerState.selectedStartDateMillis
    val end = pickerState.selectedEndDateMillis
    val datesText = if (start != null && end != null) {
        val format = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ru")).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val days = TimeUnit.MILLISECONDS.toDays(end - start).toInt().coerceAtLeast(1)
        stringResource(
            R.string.text_5_13,
            format.format(Date(start)),
            format.format(Date(end)),
            pluralStringResource(R.plurals.days_count, days, days)
        )
    } else {
        stringResource(R.string.text_5_6)
    }

    if (showDatePicker) {
        val localeRu = remember { Locale.forLanguageTag("ru-RU") }
        DisposableEffect(Unit) {
            val oldLocale = Locale.getDefault()
            Locale.setDefault(localeRu)
            onDispose {
                Locale.setDefault(oldLocale)
            }
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.text_5_14))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.text_5_17))
                }
            }
        ) {
            val baseContext = LocalContext.current
            val baseConfig = LocalConfiguration.current
            val ruConfig = remember(baseConfig) {
                Configuration(baseConfig).apply { setLocale(localeRu) }
            }
            val ruContext = remember(baseContext, ruConfig) {
                baseContext.createConfigurationContext(ruConfig)
            }
            CompositionLocalProvider(
                LocalContext provides ruContext,
                LocalConfiguration provides ruConfig
            ) {
                DateRangePicker(
                    state = pickerState,
                    title = {
                        Text(
                            text = stringResource(R.string.text_5_16),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp)
                        )
                    },
                    headline = {
                        val startMillis = pickerState.selectedStartDateMillis
                        val endMillis = pickerState.selectedEndDateMillis
                        val format = remember {
                            SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru")).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                        }
                        val headlineText = when {
                            startMillis != null && endMillis != null ->
                                "${format.format(Date(startMillis))} — ${format.format(Date(endMillis))}"
                            startMillis != null ->
                                "${format.format(Date(startMillis))} — ..."
                            else -> stringResource(R.string.text_5_18)
                        }
                        Text(
                            text = headlineText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(title = stringResource(R.string.text_5_1), onBack = onBack)

            val scrollState = rememberScrollState()
            val density = LocalDensity.current
            var topContentHeightDp by remember { mutableStateOf(0.dp) }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val dynamicCommentHeight = if (topContentHeightDp > 0.dp) {
                    (maxHeight - topContentHeightDp - 24.dp).coerceAtLeast(120.dp)
                } else {
                    120.dp
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            state = scrollState,
                            enabled = scrollState.maxValue > 0
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                topContentHeightDp = with(density) { coordinates.size.height.toDp() }
                            }
                    ) {
                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                .clickable { onSelectPet() }
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(10.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.text_5_2),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.text_5_3),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = Color.Gray
                                )
                            }

                            OutlinedButton(
                                onClick = onSelectPet,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.text_5_15),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                            Text(
                                text = stringResource(R.string.text_5_5),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .clickable { showDatePicker = true }
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = datesText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.text_5_11),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .height(dynamicCommentHeight)
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }

            PrimaryButton(
                text = stringResource(R.string.text_5_12),
                height = 56.dp,
                fontSize = 16.sp,
                onClick = onCreate
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}