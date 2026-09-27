package com.example.pet.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.theme.IBMPlexMono


@Composable
fun WelcomeScreen(onStart: () -> Unit){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.app_name),
            fontFamily = IBMPlexMono,
            fontWeight = FontWeight.Normal,
            fontSize = 64.sp
        )
        Text(text = stringResource(R.string.text_1_2), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Image(painter = painterResource(R.drawable.welcome_cat), contentDescription = null)
        Spacer(modifier = Modifier.weight(3f))
        PrimaryButton(text = stringResource(R.string.start_text),height = 80.dp, fontSize = 42.sp, onClick = onStart,  modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.weight(0.5f))
    }
}