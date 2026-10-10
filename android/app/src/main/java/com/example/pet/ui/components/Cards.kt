package com.example.pet.ui.components

import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.graphicsLayer
import com.example.pet.data.displayPersonName
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.Placeable

private val CardShape = RoundedCornerShape(16.dp)

fun Modifier.cardSurface(onClick: (() -> Unit)? = null): Modifier = composed {
    val outline = MaterialTheme.colorScheme.outline
    if (onClick == null) {
        this
            .clip(CardShape)
            .border(1.dp, outline, CardShape)
    } else {
        val interaction = remember { MutableInteractionSource() }
        this
            .pressScale(interaction, pressedScale = 0.98f)
            .clip(CardShape)
            .border(1.dp, outline, CardShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
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

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
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
            .clip(RoundedCornerShape(20.dp))
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
    size: Dp = 52.dp,
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
                    size >= 88.dp -> MaterialTheme.typography.headlineSmall
                    size >= 64.dp -> MaterialTheme.typography.titleLarge
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
    size: Dp = 64.dp,
    zoomTitle: String? = null,
    zoomable: Boolean = false
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
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "sectionChevron")
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { rotationZ = rotation }
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    large: Boolean = false,
    singleLine: Boolean = false
) {
    if (traits.isEmpty()) return
    if (singleLine) {
        SingleLineChips(labels = traits.map { stringResource(it.label) }, modifier = modifier)
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        traits.forEach { TagChip(stringResource(it.label), large = large) }
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
    itemIcon: ((T) -> ImageVector)? = null
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
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "groupArrow"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(200))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (selectedCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = selectedCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(arrowRotation)
            )
        }
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
    modifier: Modifier = Modifier
) {
    GroupedChipSelector(
        groups = PetTraitGroup.entries,
        itemsOf = { group -> PetTrait.entries.filter { it.group == group } },
        groupLabel = { stringResource(it.label) },
        itemLabel = { stringResource(it.label) },
        selected = selected,
        onToggle = onToggle,
        modifier = modifier
    )
}

@Composable
fun HomeConditionSelector(
    selected: Set<HomeConditionType>,
    onToggle: (HomeConditionType) -> Unit,
    modifier: Modifier = Modifier
) {
    GroupedChipSelector(
        groups = HomeConditionGroup.entries,
        itemsOf = { group -> HomeConditionType.entries.filter { it.group == group } },
        groupLabel = { stringResource(it.label) },
        itemLabel = { stringResource(it.label) },
        itemIcon = { it.icon },
        selected = selected,
        onToggle = onToggle,
        modifier = modifier
    )
}

@Composable
fun MyResponseStatusChip(status: MyResponseStatus) {
    val (container, content) = when (status) {
        MyResponseStatus.Pending -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        MyResponseStatus.Chosen -> MaterialTheme.extraColors.successContainer to MaterialTheme.extraColors.success
        MyResponseStatus.NotChosen, MyResponseStatus.Expired ->
            MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.onSurfaceVariant
        MyResponseStatus.Completed -> MaterialTheme.extraColors.warningContainer to MaterialTheme.extraColors.warning
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

