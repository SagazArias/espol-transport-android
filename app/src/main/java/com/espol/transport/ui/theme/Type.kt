package com.espol.transport.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.espol.transport.R

// Inter (OFL), instancias estáticas recortadas a latín. Dos cortes ópticos, como SF Text / SF Display:
// opsz 14 para lectura y opsz 32 para títulos. Estáticas porque Compose no aplicó los ejes del variable.
private val InterText = FontFamily(
    Font(R.font.inter_text_regular, FontWeight.Normal),
    Font(R.font.inter_text_medium, FontWeight.Medium),
    Font(R.font.inter_text_semibold, FontWeight.SemiBold),
    Font(R.font.inter_text_bold, FontWeight.Bold),
)
private val InterDisplay = FontFamily(
    Font(R.font.inter_display_semibold, FontWeight.SemiBold),
    Font(R.font.inter_display_bold, FontWeight.Bold),
)

// Tracking por tamaño: negativo en lo grande, ~0 en lo pequeño. Interlineado ajustado en títulos.
private fun style(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    tracking: Double,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
)

val Typography = Typography(
    displayLarge = style(InterDisplay, FontWeight.Bold, 40, 46, -0.025),
    displayMedium = style(InterDisplay, FontWeight.Bold, 34, 40, -0.022),
    displaySmall = style(InterDisplay, FontWeight.Bold, 28, 34, -0.02),
    headlineLarge = style(InterDisplay, FontWeight.Bold, 28, 34, -0.02),
    headlineMedium = style(InterDisplay, FontWeight.SemiBold, 24, 30, -0.019),
    headlineSmall = style(InterDisplay, FontWeight.SemiBold, 22, 28, -0.017),
    titleLarge = style(InterDisplay, FontWeight.SemiBold, 20, 25, -0.015),
    titleMedium = style(InterText, FontWeight.SemiBold, 17, 22, -0.011),
    titleSmall = style(InterText, FontWeight.SemiBold, 15, 20, -0.006),
    bodyLarge = style(InterText, FontWeight.Normal, 17, 24, -0.011),
    bodyMedium = style(InterText, FontWeight.Normal, 15, 21, -0.006),
    bodySmall = style(InterText, FontWeight.Normal, 13, 18, 0.0),
    labelLarge = style(InterText, FontWeight.SemiBold, 16, 21, -0.01),
    labelMedium = style(InterText, FontWeight.Medium, 13, 18, 0.0),
    labelSmall = style(InterText, FontWeight.Medium, 12, 16, 0.005),
)
