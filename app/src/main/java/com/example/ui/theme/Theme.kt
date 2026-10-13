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

private val LightColorScheme = lightColorScheme(
    primary = BilloraCrimson,
    onPrimary = Color.White,
    primaryContainer = BilloraCrimsonSoft,
    onPrimaryContainer = BilloraCrimsonDark,
    secondary = BilloraCharcoal,
    onSecondary = Color.White,
    secondaryContainer = BilloraSurfaceVariantLight,
    onSecondaryContainer = BilloraBlack,
    tertiary = BilloraCrimsonLight,
    onTertiary = Color.White,
    background = BilloraBackgroundLight,
    onBackground = BilloraBlack,
    surface = BilloraSurfaceLight,
    onSurface = BilloraBlack,
    surfaceVariant = BilloraSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF424242),
    outline = BilloraOutlineLight,
    outlineVariant = Color(0xFFEAEAEA)
)

private val DarkColorScheme = darkColorScheme(
    primary = BilloraCrimsonLight,
    onPrimary = Color.White,
    primaryContainer = BilloraCrimsonDark,
    onPrimaryContainer = BilloraCrimsonSoft,
    secondary = Color(0xFFE0E0E6),
    onSecondary = BilloraBlack,
    secondaryContainer = BilloraSurfaceVariantDark,
    onSecondaryContainer = Color.White,
    tertiary = BilloraCrimson,
    onTertiary = Color.White,
    background = BilloraBackgroundDark,
    onBackground = Color(0xFFEEEEEE),
    surface = BilloraSurfaceDark,
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = BilloraSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB0B0B8),
    outline = BilloraOutlineDark,
    outlineVariant = Color(0xFF2C2C34)
)

@Composable
fun BilloraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Preserve intentional red, black and white branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
