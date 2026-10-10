package com.example.pet.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pet.BuildConfig
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.ThemeMode
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.SectionTitle
import com.example.pet.ui.components.SegmentedToggle
import com.example.pet.ui.components.SettingsRow
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.openNotificationSettings

private val themeOptions = listOf(
    ThemeMode.System to R.string.text_22_6,
    ThemeMode.Light to R.string.text_22_7,
    ThemeMode.Dark to R.string.text_22_8
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onOpenGuide: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    val themeMode by AppContainer.settings.themeMode.collectAsStateWithLifecycle()

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
            ScreenHeader(title = stringResource(R.string.text_16_2), onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                SectionTitle(stringResource(R.string.text_22_1))

                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsRow(
                        icon = Icons.Default.Edit,
                        text = stringResource(R.string.text_16_1),
                        onClick = onEditProfile
                    )
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        text = stringResource(R.string.text_16_3),
                        onClick = onChangePassword
                    )
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        text = stringResource(R.string.text_22_9),
                        onClick = { openNotificationSettings(context) }
                    )
                }

                Spacer(Modifier.height(28.dp))

                SectionTitle(stringResource(R.string.text_22_5))

                Spacer(Modifier.height(12.dp))

                SegmentedToggle(
                    options = themeOptions.map { stringResource(it.second) },
                    selectedIndex = themeOptions.indexOfFirst { it.first == themeMode },
                    onSelect = { index -> AppContainer.settings.setThemeMode(themeOptions[index].first) }
                )

                Spacer(Modifier.height(28.dp))

                SectionTitle(stringResource(R.string.text_22_11))

                Spacer(Modifier.height(12.dp))

                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    text = stringResource(R.string.text_22_10),
                    onClick = onOpenGuide
                )

                Spacer(Modifier.height(28.dp))

                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    text = stringResource(R.string.text_16_4),
                    onClick = { showLogoutDialog = true },
                    danger = true
                )

                Spacer(Modifier.height(32.dp))
            }

            Text(
                text = stringResource(R.string.text_22_12, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.text_22_2)) },
            text = { Text(stringResource(R.string.text_22_3)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.text_22_4),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}
