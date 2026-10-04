package com.example.pet.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader

private data class PetRequest(val petName: String, val dates: String, val note: String)

// TODO: заменить на реальный список заявок владельца
private val mockRequests = listOf(
    PetRequest("Барсик", "7 мая - 15 сентября", "ул. Пушкина, дом 48"),
    PetRequest("Муся", "16 июня - 5 июля", "Муся это ты ?"),
    PetRequest("Памперс", "7 мая - 15 сентября", "ул. Горького, дом 37")
)

@Composable
fun FeedScreen(
    onBack: (() -> Unit)? = null,
    onCreateClick: () -> Unit = {},
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
            mockRequests.forEach { request ->
                RequestCard(request)
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
private fun RequestCard(request: PetRequest) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
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

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = request.petName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = request.dates,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = request.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}