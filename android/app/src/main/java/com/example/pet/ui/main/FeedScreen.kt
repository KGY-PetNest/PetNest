package com.example.pet.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pet.R
import com.example.pet.data.MockData
import com.example.pet.data.PetRequest
import com.example.pet.ui.components.IconLine
import com.example.pet.ui.components.PetThumbnail
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.TagChip
import com.example.pet.ui.components.cardSurface

@Composable
fun FeedScreen(
    onBack: (() -> Unit)? = null,
    onCreateClick: () -> Unit = {},
    onRequestClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        ScreenHeader(
            title = stringResource(R.string.text_8_1),
            onBack = onBack
        )

        Spacer(Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MockData.ownerRequests.forEach { request ->
                RequestCard(
                    request = request,
                    onClick = { onRequestClick(request.id) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.text_8_2),
            onClick = onCreateClick
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RequestCard(request: PetRequest, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface(onClick)
            .padding(12.dp)
    ) {
        PetThumbnail(size = 56.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = request.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            IconLine(icon = Icons.Default.DateRange, text = request.dates)
            IconLine(icon = Icons.Default.LocationOn, text = request.district)
        }

        TagChip(
            text = if (request.responsesCount > 0) {
                pluralStringResource(R.plurals.responses_count, request.responsesCount, request.responsesCount)
            } else {
                stringResource(R.string.text_8_3)
            }
        )
    }
}