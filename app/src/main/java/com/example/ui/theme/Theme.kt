package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IOSLightColorScheme = lightColorScheme(
    primary = IOSBlue,
    onPrimary = Color.White,
    secondary = IOSGreen,
    onSecondary = Color.White,
    tertiary = IOSOrange,
    background = Color.Black,
    surface = IOSCardBackground,
    onSurface = TextPrimaryOnLight,
    surfaceVariant = IOSLightGray,
    onSurfaceVariant = TextSecondaryOnLight
)

private val IOSDarkColorScheme = darkColorScheme(
    primary = IOSBlue,
    onPrimary = Color.White,
    secondary = IOSGreen,
    onSecondary = Color.White,
    tertiary = IOSOrange,
    background = Color.Black,
    surface = IOSDarkCardBackground,
    onSurface = TextPrimaryOnDark,
    surfaceVariant = Color(0xFF3A3A3C),
    onSurfaceVariant = TextSecondaryOnDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IOSDarkColorScheme else IOSLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
