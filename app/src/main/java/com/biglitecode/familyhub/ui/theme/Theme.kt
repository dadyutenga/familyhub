package com.biglitecode.familyhub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    secondary = GoldYellow,
    onSecondary = TextBrown,
    background = CreamBackground,
    onBackground = TextBrown,
    surface = CardCream,
    onSurface = TextBrown,
    error = CoralRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    primaryContainer = ForestGreenLight,
    onPrimaryContainer = ForestGreen,
    secondaryContainer = GoldYellowLight,
    onSecondaryContainer = TextBrown,
    outline = BorderGreen
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = TextBrown,
    secondary = GoldYellow,
    onSecondary = TextBrown,
    background = Color(0xFF1C1610),
    onBackground = CreamBackground,
    surface = Color(0xFF2A2218),
    onSurface = CreamBackground,
    error = CoralRed,
    onError = Color.White,
    errorContainer = CoralRed.copy(alpha = 0.20f),
    onErrorContainer = CreamBackground
)

@Composable
fun FamilyHubTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Force light theme for the warm family palette.
    // DarkColorScheme is kept for future use but not enabled automatically.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FamilyHubShapes,
        content = content
    )
}
