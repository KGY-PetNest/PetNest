package com.example.pet.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.theme.IBMPlexMono

@Composable
fun WelcomeScreen(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.text_1_2),
                style = MaterialTheme.typography.bodyLarge.copy(
                    letterSpacing = 0.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.welcome_cat),
                contentDescription = null,
                modifier = Modifier.weight(2.5f, fill = false)
            )

            Spacer(modifier = Modifier.weight(2.5f))

            PrimaryButton(
                text = stringResource(R.string.start_text),
                onClick = onStart
            )

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}