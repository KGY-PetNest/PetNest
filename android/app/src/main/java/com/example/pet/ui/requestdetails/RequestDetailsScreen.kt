package com.example.pet.ui.requestdetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.PetTraitChips
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.adaptiveContentWidth
import kotlinx.coroutines.launch

@Composable
fun RequestDetailsScreen(
    requestId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val feed by AppContainer.requests.feed.collectAsStateWithLifecycle()
    val responded by AppContainer.requests.respondedIds.collectAsStateWithLifecycle()
    val request = feed.firstOrNull { it.id == requestId } ?: return
    val isResponded = request.id in responded
    var busy by remember { mutableStateOf(false) }
    val bodyStyle = MaterialTheme.typography.bodyLarge

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(title = stringResource(R.string.text_21_1), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PetThumbnail(size = 88.dp)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = request.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = request.petInfo,
                            style = bodyStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        icon = Icons.Default.LocationOn,
                        text = request.place,
                        iconSize = 22.dp,
                        textStyle = bodyStyle,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    if (request.ownerName.isNotBlank()) {
                        IconLine(
                            icon = Icons.Default.Person,
                            text = stringResource(R.string.text_21_2, request.ownerName),
                            iconSize = 22.dp,
                            textStyle = bodyStyle,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (request.traits.isNotEmpty() || request.features.isNotBlank()) {
                    Spacer(Modifier.height(24.dp))
                    SectionTitle(stringResource(R.string.text_4_6))
                    Spacer(Modifier.height(10.dp))
                    PetTraitChips(traits = request.traits, large = true)
                    if (request.features.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(text = request.features, style = bodyStyle)
                    }
                }

                if (request.comment.isNotBlank()) {
                    Spacer(Modifier.height(24.dp))
                    SectionTitle(stringResource(R.string.text_5_11))
                    Spacer(Modifier.height(8.dp))
                    Text(text = request.comment, style = bodyStyle)
                }

                Spacer(Modifier.height(24.dp))
            }

            AnimatedContent(
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
                            textColor = MaterialTheme.colorScheme.primary
                        )
                        TextButton(
                            enabled = !busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    AppContainer.requests.cancelResponse(request.id)
                                    busy = false
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.text_21_5),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        PrimaryButton(
                            text = stringResource(R.string.text_21_3),
                            loading = busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    AppContainer.requests.respond(request.id)
                                    busy = false
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}