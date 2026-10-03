package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VendorGreenLight,
    onPrimary = Color.Black,
    primaryContainer = VendorGreenDark,
    onPrimaryContainer = Color.White,
    secondary = SkillsOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color.White,
    tertiary = SuperBlueLight,
    onTertiary = Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
    primary = VendorGreenDark,
    onPrimary = Color.White,
    primaryContainer = VendorGreenContainer,
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = SkillsOrange,
    onSecondary = Color.White,
    secondaryContainer = SkillsOrangeContainer,
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = SuperBlue,
    onTertiary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand colors for strong identity
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
