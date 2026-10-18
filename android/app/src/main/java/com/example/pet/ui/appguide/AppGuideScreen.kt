package com.example.pet.ui.appguide

import com.example.pet.ui.components.ScreenHorizontalPadding
import com.example.pet.ui.components.ScreenContentInset
import com.example.pet.ui.components.ScreenTopGap
import com.example.pet.ui.components.ScreenBottomGap
import com.example.pet.ui.components.ScrollEndGap
import com.example.pet.ui.components.ToggleContentGap
import com.example.pet.ui.components.ListItemSpacing
import com.example.pet.ui.components.CardContentPadding
import com.example.pet.ui.components.ShortAnimMs
import com.example.pet.ui.components.PinnedBar
import com.example.pet.ui.components.InfoBanner
import com.example.pet.ui.components.ScreenSection
import com.example.pet.ui.components.ExpandChevron
import com.example.pet.ui.components.revealEnter
import com.example.pet.ui.components.revealExit
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.UserRole
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.cardSurface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import com.example.pet.ui.components.TagChip
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

private data class GuideStep(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val text: Int
)

private data class GuideQuestion(
    @param:StringRes val question: Int,
    @param:StringRes val answer: Int
)

private val ownerSteps = listOf(
    GuideStep(Icons.Default.Pets, R.string.text_26_6, R.string.text_26_7),
    GuideStep(Icons.Default.AddCircle, R.string.text_26_8, R.string.text_26_9),
    GuideStep(Icons.Default.Groups, R.string.text_26_10, R.string.text_26_11),
    GuideStep(Icons.Default.ChatBubble, R.string.text_26_12, R.string.text_26_13),
    GuideStep(Icons.Default.HowToReg, R.string.text_26_14, R.string.text_26_15),
    GuideStep(Icons.Default.Star, R.string.text_26_16, R.string.text_26_17)
)

private val volunteerSteps = listOf(
    GuideStep(Icons.Default.Person, R.string.text_26_18, R.string.text_26_19),
    GuideStep(Icons.Default.LocationOn, R.string.text_26_20, R.string.text_26_21),
    GuideStep(Icons.Default.Search, R.string.text_26_22, R.string.text_26_23),
    GuideStep(Icons.Default.ThumbUp, R.string.text_26_24, R.string.text_26_25),
    GuideStep(Icons.Default.ChatBubble, R.string.text_26_26, R.string.text_26_27),
    GuideStep(Icons.Default.CheckCircle, R.string.text_26_28, R.string.text_26_29)
)

private val ownerQuestions = listOf(
    GuideQuestion(R.string.text_26_31, R.string.text_26_32),
    GuideQuestion(R.string.text_26_33, R.string.text_26_34),
    GuideQuestion(R.string.text_26_35, R.string.text_26_36),
    GuideQuestion(R.string.text_26_37, R.string.text_26_38),
    GuideQuestion(R.string.text_26_65, R.string.text_26_66)
)

private val volunteerQuestions = listOf(
    GuideQuestion(R.string.text_26_39, R.string.text_26_40),
    GuideQuestion(R.string.text_26_41, R.string.text_26_42),
    GuideQuestion(R.string.text_26_43, R.string.text_26_44),
    GuideQuestion(R.string.text_26_45, R.string.text_26_46)
)

private val commonQuestions = listOf(
    GuideQuestion(R.string.text_26_67, R.string.text_26_68),
    GuideQuestion(R.string.text_26_69, R.string.text_26_70),
    GuideQuestion(R.string.text_26_47, R.string.text_26_48),
    GuideQuestion(R.string.text_26_49, R.string.text_26_50)
)

@Composable
fun AppGuideScreen(
    role: UserRole,
    firstRun: Boolean,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by rememberSaveable { mutableIntStateOf(if (role == UserRole.Owner) 0 else 1) }

    BackHandler(enabled = firstRun) { onFinish() }

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
                title = stringResource(R.string.text_26_1),
                onBack = if (firstRun) null else onBack
            )

            val scrollState = rememberScrollState()
            val hintThresholdPx = with(LocalDensity.current) { 48.dp.toPx() }
            val showScrollHint by remember {
                derivedStateOf { scrollState.canScrollForward && scrollState.value < hintThresholdPx }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = ScreenContentInset)
                ) {
                    Spacer(Modifier.height(ScreenTopGap))

                    if (firstRun) {
                        InfoBanner(
                            icon = Icons.Default.Lightbulb,
                            text = stringResource(R.string.text_26_52),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(ListItemSpacing))
                    }

                    SegmentedToggle(
                        options = listOf(
                            stringResource(R.string.text_26_2),
                            stringResource(R.string.text_26_3)
                        ),
                        selectedIndex = tab,
                        onSelect = { tab = it }
                    )

                    Spacer(Modifier.height(ToggleContentGap))

                    Crossfade(
                        targetState = tab,
                        animationSpec = tween(ShortAnimMs),
                        label = "guideTab"
                    ) { index ->
                        val isOwner = index == 0
                        GuideContent(
                            intro = stringResource(if (isOwner) R.string.text_26_4 else R.string.text_26_5),
                            steps = if (isOwner) ownerSteps else volunteerSteps,
                            questions = (if (isOwner) ownerQuestions else volunteerQuestions) + commonQuestions
                        )
                    }

                    Spacer(Modifier.height(if (firstRun) ScrollEndGap else ScreenBottomGap))
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showScrollHint,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    ScrollHint()
                }
            }

            if (firstRun) {
                PinnedBar(dividerVisible = scrollState.canScrollForward) {
                    PrimaryButton(
                        text = stringResource(R.string.text_26_51),
                        onClick = onFinish
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideContent(
    intro: String,
    steps: List<GuideStep>,
    questions: List<GuideQuestion>
) {
    var expanded by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = intro,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        steps.forEachIndexed { index, step ->
            StepItem(
                number = index + 1,
                step = step,
                isLast = index == steps.lastIndex
            )
        }

        ScreenSection(title = stringResource(R.string.text_26_53)) {
            NavigationHelp()
        }

        ScreenSection(title = stringResource(R.string.text_26_30)) {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemSpacing)) {
                questions.forEachIndexed { index, item ->
                    QuestionItem(
                        item = item,
                        expanded = expanded == index,
                        onToggle = { expanded = if (expanded == index) null else index }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    number: Int,
    step: GuideStep,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(1.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 20.dp)
        ) {
            Row(
                modifier = Modifier.height(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(step.title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(step.text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuestionItem(
    item: GuideQuestion,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface(onClick = onToggle)
            .padding(CardContentPadding)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(item.question),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            ExpandChevron(expanded = expanded)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = revealEnter(),
            exit = revealExit()
        ) {
            Text(
                text = stringResource(item.answer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private enum class NavKey { Back, Home, Recents }

@Composable
private fun NavigationHelp() {
    val density = LocalDensity.current
    val usesButtons = WindowInsets.tappableElement.getBottom(density) > 0

    Column(verticalArrangement = Arrangement.spacedBy(ListItemSpacing)) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .cardSurface()
                .padding(CardContentPadding)
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.text_26_54),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.text_26_55),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .cardSurface()
                .padding(CardContentPadding)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.text_26_56),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.text_26_57),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
            ) {
                NavModeTile(
                    title = stringResource(R.string.text_26_58),
                    caption = stringResource(R.string.text_26_59),
                    current = !usesButtons,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    GesturePhone()
                }
                NavModeTile(
                    title = stringResource(R.string.text_26_60),
                    caption = stringResource(R.string.text_26_61),
                    current = usesButtons,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NavKeysBar(listOf(NavKey.Back, NavKey.Home, NavKey.Recents))
                        NavKeysBar(listOf(NavKey.Recents, NavKey.Home, NavKey.Back))
                    }
                }
            }

            Text(
                text = stringResource(R.string.text_26_63),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NavModeTile(
    title: String,
    caption: String,
    current: Boolean,
    modifier: Modifier = Modifier,
    illustration: @Composable () -> Unit
) {
    val borderColor = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(if (current) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (current) {
                Spacer(Modifier.width(6.dp))
                TagChip(text = stringResource(R.string.text_26_62))
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(96.dp)
        ) {
            illustration()
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GesturePhone() {
    val frame = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 96.dp)
            .border(2.dp, frame, RoundedCornerShape(12.dp))
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = accent,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp)
                .size(20.dp)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = accent,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp)
                .size(20.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .size(width = 22.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(frame)
        )
    }
}

@Composable
private fun NavKeysBar(keys: List<NavKey>) {
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        keys.forEach { key ->
            Canvas(modifier = Modifier.size(16.dp)) {
                val stroke = Stroke(width = 2.dp.toPx())
                val color = if (key == NavKey.Back) accent else muted
                val inset = 2.dp.toPx()
                when (key) {
                    NavKey.Back -> {
                        val path = Path().apply {
                            moveTo(size.width - inset, inset)
                            lineTo(inset, size.height / 2)
                            lineTo(size.width - inset, size.height - inset)
                            close()
                        }
                        drawPath(path = path, color = color, style = stroke)
                    }
                    NavKey.Home -> drawCircle(color = color, radius = size.minDimension / 2 - inset, style = stroke)
                    NavKey.Recents -> drawRoundRect(
                        color = color,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2, size.height - inset * 2),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                        style = stroke
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollHint() {
    val transition = rememberInfiniteTransition(label = "scrollHint")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "scrollHintBounce"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.text_26_64),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(20.dp)
                .offset(y = bounce.dp)
        )
    }
}
