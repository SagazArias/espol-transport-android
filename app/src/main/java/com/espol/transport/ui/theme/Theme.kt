package com.espol.transport.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Paleta estilo Apple: fondo agrupado gris, tarjetas blancas, un solo acento (azul ESPOL).
// Las barras (surface) usan el mismo color del fondo para que no se vean como franjas.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0A5BD3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EDFC),
    onPrimaryContainer = Color(0xFF0A3A86),
    inversePrimary = Color(0xFF6AA3FF),
    secondary = Color(0xFF3A5A8C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EDFC),
    onSecondaryContainer = Color(0xFF0A3A86),
    tertiary = Color(0xFF248A3D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF3E2),
    onTertiaryContainer = Color(0xFF0E4A1E),
    error = Color(0xFFD70015),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E3),
    onErrorContainer = Color(0xFF8A0010),
    background = Color(0xFFF2F2F7),
    onBackground = Color.Black,
    surface = Color(0xFFF2F2F7),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF6C6C70),
    surfaceTint = Color.Transparent, // sin tinte azul por elevación
    inverseSurface = Color(0xFF1C1C1E),
    inverseOnSurface = Color.White,
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFFC6C6C8),
    scrim = Color.Black,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE5E5EA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF9F9FB),
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White, // Card
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6AA3FF),
    onPrimary = Color(0xFF00214F),
    primaryContainer = Color(0xFF0D2E5C),
    onPrimaryContainer = Color(0xFFD6E4FF),
    inversePrimary = Color(0xFF0A5BD3),
    secondary = Color(0xFFA9C2EB),
    onSecondary = Color(0xFF0E2A55),
    secondaryContainer = Color(0xFF0D2E5C),
    onSecondaryContainer = Color(0xFFD6E4FF),
    tertiary = Color(0xFF30D158),
    onTertiary = Color(0xFF00391A),
    tertiaryContainer = Color(0xFF0F3D1D),
    onTertiaryContainer = Color(0xFFC8F5D3),
    error = Color(0xFFFF6961),
    onError = Color(0xFF5C0008),
    errorContainer = Color(0xFF5C1414),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFFF2F2F7),
    inverseOnSurface = Color.Black,
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF38383A),
    scrim = Color.Black,
    surfaceBright = Color(0xFF2C2C2E),
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color(0xFF0E0E10),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E), // diálogos, un nivel sobre las tarjetas
    surfaceContainerHighest = Color(0xFF1C1C1E), // Card
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp), // campos de texto
    small = RoundedCornerShape(12.dp), // chips
    medium = RoundedCornerShape(16.dp), // tarjetas
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp), // diálogos y sheets
)

@Composable
fun EspolTransportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
