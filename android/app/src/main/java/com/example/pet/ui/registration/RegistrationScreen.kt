package com.example.pet.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pet.R
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.SegmentButton
import com.example.pet.ui.theme.IBMPlexMono

@Composable
fun RegistrationScreen(
    onBack: () -> Unit,
    onLoginClick: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(Modifier.height(96.dp))

        Text(
            text = stringResource(R.string.text_3_1),
            fontSize = 28.sp,
            fontFamily = IBMPlexMono,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(Modifier.height(20.dp))

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

        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val fieldModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.text_3_2)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = fieldModifier,
                maxLines = 1
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text(stringResource(R.string.text_3_3)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp),
                modifier = fieldModifier,
                maxLines = 1
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(stringResource(R.string.text_2_4)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                modifier = fieldModifier,
                maxLines = 1
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(stringResource(R.string.text_2_5)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                modifier = fieldModifier,
                maxLines = 1
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = stringResource(R.string.text_3_4),
            height = 56.dp,
            fontSize = 16.sp,
            onClick = onSuccess
        )

        Spacer(Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = stringResource(R.string.text_3_5),
                    color = Color.Gray
                )
                Text(
                    text = stringResource(R.string.text_2_6),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.text_2_9),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* TODO */ },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(48.dp)
        )

        Spacer(Modifier.height(32.dp))
    }
}
