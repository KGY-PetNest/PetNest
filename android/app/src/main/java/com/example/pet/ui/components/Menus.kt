package com.example.pet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.pet.R
import kotlinx.coroutines.launch

private val MenuCorner = 16.dp
private val MenuShape = RoundedCornerShape(bottomStart = MenuCorner, bottomEnd = MenuCorner)
private val MenuMinWidth = 200.dp
private val FilterMenuWidth = 220.dp
private val MenuItemPadding = PaddingValues(horizontal = 16.dp)
private val AttachedMenuGap = 8.dp
private val AttachedMenuFillet = 8.dp

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(0.dp, 4.dp),
        shape = MenuShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        modifier = modifier.widthIn(min = MenuMinWidth),
        content = content
    )
}

@Composable
fun MoreMenuButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    buttonSize: Dp = 48.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(ShortAnimMs),
        label = "moreMenuButton"
    )
    Box(modifier = modifier) {
        IconButton(
            onClick = { onExpandedChange(true) },
            colors = IconButtonDefaults.iconButtonColors(containerColor = background),
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.common_more),
                tint = tint
            )
        }
        AppDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            content = content
        )
    }
}

@Composable
fun ChoiceMenuItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        trailingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        } else {
            null
        },
        contentPadding = MenuItemPadding,
        onClick = onClick
    )
}

@Composable
fun ActionMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = { Text(text = text, color = color) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        contentPadding = MenuItemPadding,
        onClick = onClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuFilterPill(
    text: String,
    selected: Boolean,
    leadingIcon: ImageVector,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    edgeMargin: Dp = ScreenHorizontalPadding,
    menu: @Composable ColumnScope.() -> Unit
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val marginPx = with(density) { edgeMargin.toPx() }
    val minMenuWidthPx = with(density) { FilterMenuWidth.toPx() }
    val windowWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    var pillSize by remember { mutableStateOf(IntSize.Zero) }
    var left by remember { mutableFloatStateOf(0f) }
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = expanded
    val pillBottomCorner by animateDpAsState(
        targetValue = if (expanded) 0.dp else FilterPillCorner,
        animationSpec = tween(ShortAnimMs),
        label = "pillBottomCorner"
    )
    val container = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier
            .bringIntoViewRequester(requester)
            .onGloballyPositioned {
                pillSize = it.size
                left = it.boundsInWindow().left
            }
    ) {
        FilterPill(
            text = text,
            selected = selected && !expanded,
            leadingIcon = leadingIcon,
            shape = RoundedCornerShape(
                topStart = FilterPillCorner,
                topEnd = FilterPillCorner,
                bottomStart = pillBottomCorner,
                bottomEnd = pillBottomCorner
            ),
            onClick = {
                if (expanded) {
                    onExpandedChange(false)
                } else {
                    scope.launch {
                        requester.bringIntoView(
                            Rect(-marginPx, 0f, maxOf(pillSize.width.toFloat(), minMenuWidthPx) + marginPx, pillSize.height.toFloat())
                        )
                        onExpandedChange(true)
                    }
                }
            }
        )

        if (visibleState.currentState || visibleState.targetState) {
            val pillWidth = pillSize.width.toFloat()
            val menuWidthPx = maxOf(pillWidth, minMenuWidthPx)
            val alignEnd = left + menuWidthPx > windowWidthPx - marginPx
            val wider = menuWidthPx > pillWidth
            val tabStart = if (alignEnd) menuWidthPx - pillWidth else 0f
            val tabEnd = tabStart + pillWidth
            val menuShape = RoundedCornerShape(
                topStart = if (wider && alignEnd) MenuCorner else 0.dp,
                topEnd = if (wider && !alignEnd) MenuCorner else 0.dp,
                bottomStart = MenuCorner,
                bottomEnd = MenuCorner
            )
            Popup(
                popupPositionProvider = remember(alignEnd) { AttachedMenuPosition(alignEnd) },
                onDismissRequest = { onExpandedChange(false) },
                properties = PopupProperties(focusable = true)
            ) {
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = expandVertically(tween(ShortAnimMs), expandFrom = Alignment.Top) + fadeIn(tween(ShortAnimMs)),
                    exit = shrinkVertically(tween(ShortAnimMs), shrinkTowards = Alignment.Top) + fadeOut(tween(ShortAnimMs))
                ) {
                    Column(modifier = Modifier.width(with(density) { menuWidthPx.toDp() })) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AttachedMenuGap)
                        ) {
                            drawRect(
                                color = container,
                                topLeft = Offset(tabStart, 0f),
                                size = Size(pillWidth, size.height)
                            )
                            if (wider) {
                                val r = AttachedMenuFillet.toPx()
                                val bottom = size.height
                                val path = Path().apply {
                                    if (alignEnd) {
                                        moveTo(tabStart, bottom - r)
                                        lineTo(tabStart, bottom)
                                        lineTo(tabStart - r, bottom)
                                        arcTo(
                                            rect = Rect(Offset(tabStart - r, bottom - r), r),
                                            startAngleDegrees = 90f,
                                            sweepAngleDegrees = -90f,
                                            forceMoveTo = false
                                        )
                                    } else {
                                        moveTo(tabEnd, bottom - r)
                                        lineTo(tabEnd, bottom)
                                        lineTo(tabEnd + r, bottom)
                                        arcTo(
                                            rect = Rect(Offset(tabEnd + r, bottom - r), r),
                                            startAngleDegrees = 90f,
                                            sweepAngleDegrees = 90f,
                                            forceMoveTo = false
                                        )
                                    }
                                    close()
                                }
                                drawPath(path = path, color = container)
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(menuShape)
                                .background(container)
                                .padding(vertical = 8.dp),
                            content = menu
                        )
                    }
                }
            }
        }
    }
}

private class AttachedMenuPosition(private val alignEnd: Boolean) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = if (alignEnd) anchorBounds.right - popupContentSize.width else anchorBounds.left
        return IntOffset(x, anchorBounds.bottom)
    }
}
