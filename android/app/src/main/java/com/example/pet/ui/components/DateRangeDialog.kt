package com.example.pet.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pet.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberFutureDateRangePickerState(): DateRangePickerState {
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
    return rememberDateRangePickerState(
        initialDisplayMode = DisplayMode.Picker,
        selectableDates = remember(todayStartUtc, currentYear) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis >= todayStartUtc

                override fun isSelectableYear(year: Int): Boolean = year >= currentYear
            }
        }
    )
}

private const val MILLIS_IN_DAY = 86_400_000L

fun utcMillisToLocalDate(millis: Long): LocalDate =
    LocalDate.ofEpochDay(Math.floorDiv(millis, MILLIS_IN_DAY))

fun LocalDate.toUtcMillis(): Long = toEpochDay() * MILLIS_IN_DAY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(
    state: DateRangePickerState,
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onReset: (() -> Unit)? = null
) {
    val localeRu = remember { Locale.forLanguageTag("ru-RU") }
    DisposableEffect(Unit) {
        val oldLocale = Locale.getDefault()
        Locale.setDefault(localeRu)
        onDispose { Locale.setDefault(oldLocale) }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = state.selectedEndDateMillis != null
            ) {
                Text(stringResource(R.string.common_done))
            }
        },
        dismissButton = {
            Row {
                if (onReset != null) {
                    TextButton(onClick = onReset) {
                        Text(stringResource(R.string.common_reset))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
                }
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
                state = state,
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp)
                    )
                },
                headline = {
                    val startMillis = state.selectedStartDateMillis
                    val endMillis = state.selectedEndDateMillis
                    val format = remember {
                        SimpleDateFormat("dd.MM.yyyy", localeRu).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                    }
                    val headlineText = when {
                        startMillis != null && endMillis != null ->
                            "${format.format(Date(startMillis))} — ${format.format(Date(endMillis))}"
                        startMillis != null ->
                            "${format.format(Date(startMillis))} — ..."
                        else -> stringResource(R.string.common_date_range_hint)
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