package com.espol.transport.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Navy = Color(0xFF0D47A1)
private val NavyLight = Color(0xFF5472D3)
private val Background = Color(0xFFF5F7FB)

private val Colors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondary = NavyLight,
    onSecondary = Color.White,
    tertiary = Color(0xFF00695C),
    background = Background,
    surface = Color.White,
)

@Composable
fun EspolTransportTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Typography, content = content)
}
