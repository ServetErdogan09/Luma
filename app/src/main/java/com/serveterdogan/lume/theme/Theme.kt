package com.serveterdogan.lume.theme

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
    primary = DarkPrimary,
    onPrimary = DarkOnSurface,
    primaryContainer = DarkSurfaceLight,
    onPrimaryContainer = DarkOnSurfaceVariant,

    secondary = DarkSecondary,
    onSecondary = DarkOnSurface,
    secondaryContainer = DarkSurfaceLight,
    onSecondaryContainer = DarkOnSurfaceVariant,

    tertiary = DarkAccent,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurfaceLight,
    onTertiaryContainer = DarkOnSurfaceVariant,

    background = DarkBackground,
    onBackground = DarkOnSurface,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceLight,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurface,
    surfaceContainerHighest = DarkSurfaceLight,

    error = CosmicDanger,
    errorContainer = CosmicDangerSurface,
    onError = DarkOnSurface,

    outline = DarkSurfaceLight,
    outlineVariant = DarkSurfaceLight,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightSurface,
    primaryContainer = LightPrimaryLight,
    onPrimaryContainer = LightPrimary,

    secondary = LightSecondary,
    onSecondary = LightOnSurface,
    secondaryContainer = LightSurfaceLight,
    onSecondaryContainer = LightOnSurface,

    tertiary = LightAccent,
    onTertiary = LightOnSurface,
    tertiaryContainer = LightSurfaceLight,
    onTertiaryContainer = LightOnSurface,

    background = LightBackground,
    onBackground = LightOnSurface,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceLight,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightSurfaceLight,

    error = CosmicDanger,
    errorContainer = CosmicDangerSurfaceLight,
    onError = LightSurface,

    outline = LightSurfaceLight,
    outlineVariant = LightSurfaceLight,
)

@Composable
fun LumeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        content = {
            androidx.compose.material3.ProvideTextStyle(
                value = androidx.compose.ui.text.TextStyle(fontFamily = PoppinsFontFamily),
                content = content
            )
        }
    )
}
