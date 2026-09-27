package com.example.pet.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.pet.R


@Composable
fun LoginScreen(onRegisterClick: () -> Unit, onBack: () -> Unit, onSuccess: () -> Unit){
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Text(text = stringResource(R.string.text_2_1), style = MaterialTheme.typography.headlineLarge)
    }
}