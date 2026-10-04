package com.example.pet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PetPurple,
    primaryContainer = PetPurpleLight,
    background = PetWhite,
    surface = PetWhite,
    onSurface = PetDarkText,
    onSurfaceVariant = PetGray,
    outline = PetLightGray
)

@Composable
fun PetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}