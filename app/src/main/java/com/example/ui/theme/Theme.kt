package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoffeeBrownPrimary,
    onPrimary = CoffeeBrownOnPrimary,
    primaryContainer = CoffeeContainer,
    onPrimaryContainer = OnCoffeeContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    background = WarmCreamBg,
    surface = WarmSurface,
    surfaceVariant = WarmSurfaceVariant,
    onBackground = OnWarmSurface,
    onSurface = OnWarmSurface,
    onSurfaceVariant = OnWarmSurfaceVariant,
    error = RedError,
    errorContainer = RedContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberSecondary,
    onPrimary = Color.Black,
    primaryContainer = CoffeeBrownPrimary,
    onPrimaryContainer = CoffeeContainer,
    secondary = AmberContainer,
    onSecondary = OnAmberContainer,
    background = DarkCoffeeBg,
    surface = DarkCoffeeSurface,
    surfaceVariant = DarkCoffeeSurfaceVariant,
    onBackground = WarmCreamBg,
    onSurface = WarmCreamBg,
    onSurfaceVariant = WarmSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    preset: ThemePreset = AppThemes.MIDNIGHT_GOLD,
    themeMode: ThemeMode = ThemeMode.DARK,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) preset.darkColors else preset.lightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
