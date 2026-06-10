package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPink,
    tertiary = NeonGreen,
    background = DeepSpaceBackground,
    surface = DeepSpaceSurface,
    onPrimary = DeepSpaceBackground,
    onSecondary = LightSlate,
    onTertiary = DeepSpaceBackground,
    onBackground = LightSlate,
    onSurface = LightSlate
)

private val LightColorScheme = lightColorScheme(
    primary = Cyan40,
    secondary = Pink40,
    tertiary = Green40,
    background = LightSlate,
    surface = DeepSpaceSurface,
    onPrimary = LightSlate,
    onSecondary = DeepSpaceBackground,
    onTertiary = LightSlate,
    onBackground = DeepSpaceBackground,
    onSurface = LightSlate
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default as it matches the high-tech assistant vibe
    dynamicColor: Boolean = false, // Disable dynamic colors to maintain our stunning cyber thematic palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
