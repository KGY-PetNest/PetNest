package com.example.pet.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pet.R

val Nunito = FontFamily(
    Font(R.font.nunito_regular, weight = FontWeight.Normal),
    Font(R.font.nunito_medium, weight = FontWeight.Medium),
    Font(R.font.nunito_semibold, weight = FontWeight.SemiBold),
    Font(R.font.nunito_bold, weight = FontWeight.Bold),
    Font(R.font.nunito_extrabold, weight = FontWeight.ExtraBold)
)

private fun style(size: Int, lineHeight: Int, weight: FontWeight, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = Nunito,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val Typography = Typography(
    displayLarge = style(56, 64, FontWeight.ExtraBold),
    displayMedium = style(44, 52, FontWeight.ExtraBold),
    displaySmall = style(36, 44, FontWeight.ExtraBold),
    headlineLarge = style(32, 40, FontWeight.ExtraBold),
    headlineMedium = style(28, 36, FontWeight.ExtraBold),
    headlineSmall = style(24, 32, FontWeight.Bold),
    titleLarge = style(22, 28, FontWeight.Bold),
    titleMedium = style(17, 24, FontWeight.Bold),
    titleSmall = style(15, 20, FontWeight.Bold),
    bodyLarge = style(16, 24, FontWeight.Normal, 0.15),
    bodyMedium = style(14, 20, FontWeight.Normal, 0.15),
    bodySmall = style(12, 16, FontWeight.Medium, 0.2),
    labelLarge = style(15, 20, FontWeight.Bold, 0.1),
    labelMedium = style(13, 16, FontWeight.Bold, 0.2),
    labelSmall = style(12, 16, FontWeight.SemiBold, 0.2)
)