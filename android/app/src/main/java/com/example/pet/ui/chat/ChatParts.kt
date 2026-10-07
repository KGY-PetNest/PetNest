package com.example.pet.ui.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.example.pet.R
import com.example.pet.data.DayMonthFormat
import com.example.pet.data.DayMonthYearFormat
import com.example.pet.data.MessageStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Ru: Locale = Locale.forLanguageTag("ru")
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Ru)
private val WeekdayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EE", Ru)
private val ShortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy", Ru)

fun formatMessageTime(time: LocalDateTime): String = time.format(TimeFormat)

fun formatChatListTime(time: LocalDateTime, today: LocalDate = LocalDate.now()): String {
    val date = time.toLocalDate()
    return when {
        date == today -> time.format(TimeFormat)
        date.isAfter(today.minusDays(7)) -> date.format(WeekdayFormat)
        else -> date.format(ShortDateFormat)
    }
}

@Composable
fun chatDayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when {
    date == today -> stringResource(R.string.text_10_7)
    date == today.minusDays(1) -> stringResource(R.string.text_10_8)
    date.year == today.year -> date.format(DayMonthFormat)
    else -> date.format(DayMonthYearFormat)
}

@Composable
fun MessageStatusIcon(
    status: MessageStatus,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    val icon = when (status) {
        MessageStatus.Sending -> Icons.Default.Schedule
        MessageStatus.Sent -> Icons.Default.Done
        MessageStatus.Read -> Icons.Default.DoneAll
        MessageStatus.Failed -> Icons.Default.ErrorOutline
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (status == MessageStatus.Failed) MaterialTheme.colorScheme.error else tint,
        modifier = modifier.size(size)
    )
}
