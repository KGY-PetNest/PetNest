package com.example.pet.ui.components

import androidx.compose.animation.AnimatedVisibility
import com.example.pet.data.displayPersonName
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.HomeConditionGroup
import com.example.pet.data.HomeConditionType
import com.example.pet.data.MyResponseStatus
import com.example.pet.data.PetTrait
import com.example.pet.data.PetTraitGroup
import com.example.pet.ui.theme.PetStar
import com.example.pet.ui.theme.extraColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.CalendarMonth
import com.example.pet.data.MonthYearFormat
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.Placeable

private val CardShape = RoundedCornerShape(16.dp)

fun Modifier.cardSurface(onClick: (() -> Unit)? = null): Modifier =
    selectableCardSurface(onClick = onClick, onLongClick = null, selected = false)

fun Modifier.cardSurface(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    selected: Boolean
): Modifier = selectableCardSurface(onClick = onClick, onLongClick = onLongClick, selected = selected)

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.selectableCardSurface(
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    selected: Boolean
): Modifier = composed {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(ShortAnimMs),
        label = "cardBorder"
    )
    val fillColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(ShortAnimMs),
        label = "cardFill"
    )
    val interaction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val surface = (if (onClick == null) this else this.pressScale(interaction, pressedScale = 0.98f))
        .clip(CardShape)
        .background(fillColor)
        .border(1.dp, borderColor, CardShape)
    if (onClick == null) {
        surface
    } else {
        surface.combinedClickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick?.let { longClick ->
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    longClick()
                }
            }
        )
    }
}

@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        style = if (large) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
        color = contentColor,
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 12.dp else 10.dp))
            .background(containerColor)
            .padding(
                horizontal = if (large) 12.dp else 10.dp,
                vertical = if (large) 6.dp else 4.dp
            )
    )
}

val FilterPillCorner = 20.dp

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(FilterPillCorner)
) {
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(200),
        label = "pillContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(200),
        label = "pillContent"
    )
    val interaction = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .pressScale(interaction)
            .clip(shape)
            .background(container)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        }
        Text(text = text, color = content, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = PersonThumbnailSize,
    photoUri: String? = null,
    zoomable: Boolean = false
) {
    val shownName = displayPersonName(name)
    val initials = shownName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    val photo = rememberPhoto(photoUri)
    var viewerOpen by rememberSaveable { mutableStateOf(false) }
    if (viewerOpen && photoUri != null) {
        PhotoViewerDialog(photoUri = photoUri, title = shownName, onDismiss = { viewerOpen = false })
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (zoomable && photo != null) Modifier.clickable { viewerOpen = true } else Modifier
            )
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
    ) {
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.primary,
                style = when {
                    size >= ProfileAvatarSize -> MaterialTheme.typography.headlineSmall
                    size >= ListThumbnailSize -> MaterialTheme.typography.titleLarge
                    else -> MaterialTheme.typography.titleMedium
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PetThumbnail(
    modifier: Modifier = Modifier,
    photoUri: String? = null,
    size: Dp = ListThumbnailSize,
    zoomTitle: String? = null,
    zoomable: Boolean = false,
    bordered: Boolean = false
) {
    val photo = rememberPhoto(photoUri)
    var viewerOpen by rememberSaveable { mutableStateOf(false) }
    if (viewerOpen && photoUri != null) {
        PhotoViewerDialog(photoUri = photoUri, title = zoomTitle, onDismiss = { viewerOpen = false })
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (zoomable && photo != null) Modifier.clickable { viewerOpen = true } else Modifier
            )
            .background(MaterialTheme.colorScheme.primaryContainer)
            .then(
                if (bordered) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier
            )
    ) {
        val bitmap = photo
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

@Composable
fun RatingLabel(
    rating: Double,
    reviewsCount: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = PetStar,
            modifier = Modifier.size(if (large) 20.dp else 16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = if (reviewsCount > 0) {
                stringResource(R.string.text_13_8, rating, reviewsCount)
            } else {
                stringResource(R.string.text_13_9)
            },
            style = if (large) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun IconLine(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines: Int = Int.MAX_VALUE
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = textStyle,
            color = textColor,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
fun SectionDivider() {
    Spacer(Modifier.height(SectionDividerTopGap))
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(SectionDividerBottomGap))
}

@Composable
fun ScreenSection(title: String, content: @Composable () -> Unit) {
    SectionDivider()
    SectionTitle(text = title)
    Spacer(Modifier.height(SectionTitleGap))
    content()
}

@Composable
fun FieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    val errorColor = MaterialTheme.colorScheme.error
    Text(
        text = buildAnnotatedString {
            append(text)
            if (required) {
                withStyle(SpanStyle(color = errorColor)) { append(" *") }
            }
        },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(start = FieldLabelInset, bottom = 4.dp)
    )
}

@Composable
fun FieldHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = FieldLabelInset, end = FieldLabelInset, bottom = 8.dp)
    )
}

@Composable
fun FieldError(visible: Boolean, text: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = revealEnter(),
        exit = revealExit()
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier.padding(start = FieldSupportingInset, top = 4.dp)
        )
    }
}

@Composable
fun FieldSupportingText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = FieldSupportingInset, end = FieldSupportingInset, top = 6.dp)
    )
}

@Composable
fun InfoBanner(
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null
) {
    Row(
        verticalAlignment = if (title != null && text != null) Alignment.Top else Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(containerColor)
            .padding(CardContentPadding)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
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

@Composable
fun IconCircle(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(IconCircleSize)
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
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    bottomSpace: Dp = 0.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = ScreenContentInset, vertical = 24.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (bottomSpace > 0.dp) {
            Spacer(Modifier.height(bottomSpace))
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CollapsibleSectionHeader(title = title, expanded = expanded, onToggle = onToggle)
        AnimatedVisibility(
            visible = expanded,
            enter = revealEnter(),
            exit = revealExit()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
                modifier = Modifier.padding(top = 4.dp),
                content = content
            )
        }
    }
}

@Composable
fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onToggle)
            .padding(horizontal = ScreenContentInset, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        ExpandChevron(expanded = expanded)
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    val color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (danger) color else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.weight(1f)
        )
        if (!danger) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PetTraitChips(
    traits: List<PetTrait>,
    modifier: Modifier = Modifier,
    other: String = "",
    large: Boolean = false,
    singleLine: Boolean = false
) {
    val labels = traits.map { stringResource(it.label) } + splitOtherItems(other)
    if (labels.isEmpty()) return
    if (singleLine) {
        SingleLineChips(labels = labels, modifier = modifier)
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        labels.forEach { TagChip(it, large = large) }
    }
}

@Composable
fun <T> SelectableChips(
    items: List<T>,
    selected: Set<T>,
    label: @Composable (T) -> String,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector)? = null
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items.forEach { item ->
            FilterPill(
                text = label(item),
                selected = item in selected,
                onClick = { onToggle(item) },
                leadingIcon = icon?.invoke(item)
            )
        }
    }
}

@Composable
fun <G, T> GroupedChipSelector(
    groups: List<G>,
    itemsOf: (G) -> List<T>,
    groupLabel: @Composable (G) -> String,
    itemLabel: @Composable (T) -> String,
    selected: Set<T>,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemIcon: ((T) -> ImageVector)? = null,
    other: OtherOption? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        groups.forEachIndexed { index, group ->
            key(group) {
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
                ChipGroupSection(
                    title = groupLabel(group),
                    items = itemsOf(group),
                    itemLabel = itemLabel,
                    itemIcon = itemIcon,
                    selected = selected,
                    onToggle = onToggle
                )
            }
        }
        if (other != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            OtherOptionSection(other)
        }
    }
}

private const val OTHER_ITEMS_MAX = 5

fun splitOtherItems(text: String): List<String> =
    text.split(',', ';', '\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { item -> item.replaceFirstChar { it.uppercase() } }
        .distinct()
        .take(OTHER_ITEMS_MAX)

class OtherOption(
    val value: String,
    val onValueChange: (String) -> Unit,
    val placeholder: String
)

@Composable
private fun OtherOptionSection(option: OtherOption) {
    var expanded by rememberSaveable { mutableStateOf(option.value.isNotBlank()) }
    var openedByUser by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(ShortAnimMs))
    ) {
        ChipGroupHeader(
            title = stringResource(R.string.common_other_option),
            count = if (option.value.isNotBlank()) 1 else 0,
            expanded = expanded,
            onClick = {
                expanded = !expanded
                openedByUser = expanded
            }
        )
        if (expanded) {
            OtherDetailsField(
                visible = true,
                value = option.value,
                onValueChange = option.onValueChange,
                placeholder = option.placeholder,
                focusOnShow = openedByUser,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp)
            )
        } else if (option.value.isNotBlank()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp)
            ) {
                splitOtherItems(option.value).forEach { item ->
                    FilterPill(
                        text = item,
                        selected = true,
                        onClick = { expanded = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipGroupHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        ExpandChevron(expanded = expanded)
    }
}

@Composable
fun OtherDetailsField(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    focusOnShow: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }
    AnimatedVisibility(
        visible = visible,
        enter = revealEnter(),
        exit = revealExit(),
        modifier = modifier
    ) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            errorText = errorText,
            maxLength = FormRules.DEFAULT_MAX_LENGTH,
            imeAction = ImeAction.Done,
            modifier = Modifier.focusRequester(focusRequester)
        )
        LaunchedEffect(Unit) {
            if (focusOnShow && value.isBlank()) {
                delay(ShortAnimMs.toLong())
                runCatching { focusRequester.requestFocus() }
            }
        }
    }
}

@Composable
private fun <T> ChipGroupSection(
    title: String,
    items: List<T>,
    itemLabel: @Composable (T) -> String,
    itemIcon: ((T) -> ImageVector)?,
    selected: Set<T>,
    onToggle: (T) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedCount = items.count { it in selected }
    val visibleItems = if (expanded) items else items.filter { it in selected }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(ShortAnimMs))
    ) {
        ChipGroupHeader(
            title = title,
            count = selectedCount,
            expanded = expanded,
            onClick = { expanded = !expanded }
        )
        if (visibleItems.isNotEmpty()) {
            SelectableChips(
                items = visibleItems,
                selected = selected,
                label = itemLabel,
                onToggle = onToggle,
                icon = itemIcon,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
fun PetTraitSelector(
    selected: Set<PetTrait>,
    onToggle: (PetTrait) -> Unit,
    modifier: Modifier = Modifier,
    other: OtherOption? = null
) {
    GroupedChipSelector(
        groups = PetTraitGroup.entries,
        itemsOf = { group -> PetTrait.entries.filter { it.group == group } },
        groupLabel = { stringResource(it.label) },
        itemLabel = { stringResource(it.label) },
        selected = selected,
        onToggle = onToggle,
        modifier = modifier,
        other = other
    )
}

@Composable
fun HomeConditionSelector(
    selected: Set<HomeConditionType>,
    onToggle: (HomeConditionType) -> Unit,
    modifier: Modifier = Modifier,
    other: OtherOption? = null
) {
    GroupedChipSelector(
        groups = HomeConditionGroup.entries,
        itemsOf = { group -> HomeConditionType.entries.filter { it.group == group } },
        groupLabel = { stringResource(it.label) },
        itemLabel = { stringResource(it.label) },
        itemIcon = { it.icon },
        selected = selected,
        onToggle = onToggle,
        modifier = modifier,
        other = other
    )
}

@Composable
fun RequestCardContent(
    photoUri: String?,
    title: String,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    details: @Composable ColumnScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
    ) {
        PetThumbnail(photoUri = photoUri, size = ListThumbnailSize)

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                badge?.invoke()
            }
            if (action == null) {
                details()
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f),
                        content = details
                    )
                    action()
                }
            }
        }
    }
}

@Composable
fun MyResponseStatusChip(status: MyResponseStatus) {
    val (container, content) = when (status) {
        MyResponseStatus.Pending -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        MyResponseStatus.Chosen -> MaterialTheme.extraColors.successContainer to MaterialTheme.extraColors.success
        MyResponseStatus.NotChosen, MyResponseStatus.Expired ->
            MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurfaceVariant
        MyResponseStatus.Completed -> MaterialTheme.extraColors.successContainer to MaterialTheme.extraColors.success
    }
    TagChip(
        text = stringResource(status.label),
        containerColor = container,
        contentColor = content
    )
}

@Composable
fun rememberPhoto(photoUri: String?): ImageBitmap? {
    val context = LocalContext.current
    val photo by produceState<ImageBitmap?>(initialValue = null, photoUri) {
        value = photoUri?.let { uri ->
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(uri))?.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
    return photo
}

val ProfileAvatarSize = 88.dp
val ListThumbnailSize = 64.dp
val PersonThumbnailSize = 52.dp
val SmallThumbnailSize = PersonThumbnailSize
val CompactAvatarSize = 40.dp
val IconCircleSize = 36.dp
val CardPadding = 12.dp
val CardContentPadding = 16.dp
val ListItemSpacing = 12.dp
val SectionDividerTopGap = 24.dp
val SectionDividerBottomGap = 20.dp
val SectionTitleGap = 12.dp
val FieldLabelInset = 4.dp
val FieldSupportingInset = 16.dp

@Composable
fun JoinedSinceLine(joinedAt: LocalDate, modifier: Modifier = Modifier) {
    IconLine(
        icon = Icons.Default.CalendarMonth,
        text = stringResource(R.string.common_joined_since, joinedAt.format(MonthYearFormat)),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
fun ProfileHeader(
    name: String,
    photoUri: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        InitialsAvatar(name = name, photoUri = photoUri, size = ProfileAvatarSize, zoomable = true)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = displayPersonName(name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun SingleLineChips(labels: List<String>, modifier: Modifier = Modifier) {
    val counterContainer = MaterialTheme.colorScheme.outline
    val counterContent = MaterialTheme.colorScheme.onSurfaceVariant
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val spacing = 6.dp.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val chips = labels.mapIndexed { index, label ->
            subcompose("chip$index") { TagChip(text = label) }.first().measure(loose)
        }
        val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE
        fun rowWidth(count: Int): Int =
            chips.take(count).sumOf { it.width } + spacing * (count - 1).coerceAtLeast(0)

        var shown = chips.size
        var counter: Placeable? = null
        if (rowWidth(shown) > maxWidth) {
            shown = chips.size - 1
            while (true) {
                val hidden = chips.size - shown
                val measured = subcompose("more$shown") {
                    TagChip(text = "+$hidden", containerColor = counterContainer, contentColor = counterContent)
                }.first().measure(loose)
                val gap = if (shown > 0) spacing else 0
                if (shown == 0 || rowWidth(shown) + gap + measured.width <= maxWidth) {
                    counter = measured
                    break
                }
                shown--
            }
        }
        val visible = chips.take(shown)
        val more = counter
        val gapBeforeMore = if (visible.isNotEmpty()) spacing else 0
        val height = (visible.map { it.height } + listOfNotNull(more?.height)).maxOrNull() ?: 0
        val width = (rowWidth(visible.size) + (more?.let { gapBeforeMore + it.width } ?: 0))
            .coerceAtMost(if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE)
            .coerceAtLeast(constraints.minWidth)
        layout(width, height) {
            var x = 0
            visible.forEach { placeable ->
                placeable.placeRelative(x, (height - placeable.height) / 2)
                x += placeable.width + spacing
            }
            if (more != null) {
                more.placeRelative(rowWidth(visible.size) + gapBeforeMore, (height - more.height) / 2)
            }
        }
    }
}

@Composable
fun StatTile(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .cardSurface(onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
