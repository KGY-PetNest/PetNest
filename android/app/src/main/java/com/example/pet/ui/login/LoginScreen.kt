package com.example.pet.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.PrimaryButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import com.example.pet.ui.components.SegmentButton
import com.example.pet.ui.theme.IBMPlexMono

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onRegisterClick: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by rememberSaveable { mutableStateOf(0) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.text_2_1),
            fontSize = 28.sp,
            fontFamily = IBMPlexMono,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
            )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(4.dp)
        ) {
            SegmentButton(
                text = stringResource(R.string.text_2_2),
                selected = selectedRole == 0,
                onClick = { selectedRole = 0 },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                text = stringResource(R.string.text_2_3),
                selected = selectedRole == 1,
                onClick = { selectedRole = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(stringResource(R.string.text_2_4)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text(stringResource(R.string.text_2_5)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = stringResource(R.string.text_2_6),
            height = 56.dp,
            fontSize = 16.sp,
            onClick = onSuccess
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
            ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.text_2_7),
                    color = Color.Gray,
                )
                Text(
                    text = stringResource(R.string.text_2_8),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.text_2_9),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* TODO */ },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}