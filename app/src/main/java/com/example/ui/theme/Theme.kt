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
    primary = KsebBluePrimaryDark,
    onPrimary = Color(0xFF002B5E),
    primaryContainer = KsebBlueContainerDark,
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = KsebAmberSecondaryDark,
    onSecondary = Color(0xFF452200),
    secondaryContainer = Color(0xFF653300),
    onSecondaryContainer = Color(0xFFFFDCC1),
    background = KsebSurfaceDark,
    onBackground = KsebTextPrimaryDark,
    surface = KsebCardBackgroundDark,
    onSurface = KsebTextPrimaryDark,
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = KsebTextSecondaryDark,
    outline = Color(0xFF455A64)
)

private val LightColorScheme = lightColorScheme(
    primary = KsebBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8F8),
    onPrimaryContainer = Color(0xFF042045),
    secondary = KsebAmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF4E2600),
    background = KsebSurfaceLight,
    onBackground = KsebTextPrimary,
    surface = KsebCardBackground,
    onSurface = KsebTextPrimary,
    surfaceVariant = Color(0xFFEBF0F7),
    onSurfaceVariant = KsebTextSecondary,
    outline = KsebOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // prioritize dedicated KSEB brand colors
    content: @Composable () -> Unit,
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
