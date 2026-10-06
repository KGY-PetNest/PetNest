package com.example.pet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PetPurple,
    onPrimary = PetWhite,
    primaryContainer = PetPurpleLight,
    onPrimaryContainer = PetPurple,
    background = PetWhite,
    onBackground = PetDarkText,
    surface = PetWhite,
    onSurface = PetDarkText,
    onSurfaceVariant = PetGray,
    outline = PetLightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = PetPurpleDark,
    onPrimary = PetOnPurpleDark,
    primaryContainer = PetPurpleContainerDark,
    onPrimaryContainer = PetPurpleDark,
    background = PetBackgroundDark,
    onBackground = PetTextDark,
    surface = PetBackgroundDark,
    onSurface = PetTextDark,
    onSurfaceVariant = PetGrayDark,
    outline = PetOutlineDark
)

@Immutable
data class PetExtraColors(
    val warning: Color,
    val warningContainer: Color,
    val success: Color,
    val successContainer: Color
)

private val LightExtraColors = PetExtraColors(
    warning = PetOrange,
    warningContainer = PetOrangeLight,
    success = PetGreen,
    successContainer = PetGreenLight
)

private val DarkExtraColors = PetExtraColors(
    warning = PetOrangeDark,
    warningContainer = PetOrangeContainerDark,
    success = PetGreenDark,
    successContainer = PetGreenContainerDark
)

private val LocalPetExtraColors = staticCompositionLocalOf { LightExtraColors }

val MaterialTheme.extraColors: PetExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPetExtraColors.current

@Composable
fun PetTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPetExtraColors provides if (darkTheme) DarkExtraColors else LightExtraColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}