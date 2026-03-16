package com.hntech.pickora.ui.theme

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
    primary = PickoraPrimaryDark,
    onPrimary = PickoraOnPrimaryDark,
    primaryContainer = PickoraPrimaryContainerDark,
    onPrimaryContainer = PickoraOnPrimaryContainerDark,
    secondary = PickoraSecondaryDark,
    onSecondary = PickoraOnSecondaryDark,
    secondaryContainer = PickoraSecondaryContainerDark,
    onSecondaryContainer = PickoraOnSecondaryContainerDark,
    tertiary = PickoraTertiaryDark,
    onTertiary = PickoraOnTertiaryDark,
    tertiaryContainer = PickoraTertiaryContainerDark,
    onTertiaryContainer = PickoraOnTertiaryContainerDark,
    background = PickoraBackgroundDark,
    onBackground = PickoraOnBackgroundDark,
    surface = PickoraSurfaceDark,
    onSurface = PickoraOnSurfaceDark,
    surfaceVariant = PickoraSurfaceVariantDark,
    onSurfaceVariant = PickoraOnSurfaceVariantDark,
    outline = PickoraOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = PickoraPrimary,
    onPrimary = PickoraOnPrimary,
    primaryContainer = PickoraPrimaryContainer,
    onPrimaryContainer = PickoraOnPrimaryContainer,
    secondary = PickoraSecondary,
    onSecondary = PickoraOnSecondary,
    secondaryContainer = PickoraSecondaryContainer,
    onSecondaryContainer = PickoraOnSecondaryContainer,
    tertiary = PickoraTertiary,
    onTertiary = PickoraOnTertiary,
    tertiaryContainer = PickoraTertiaryContainer,
    onTertiaryContainer = PickoraOnTertiaryContainer,
    background = PickoraBackground,
    onBackground = PickoraOnBackground,
    surface = PickoraSurface,
    onSurface = PickoraOnSurface,
    surfaceVariant = PickoraSurfaceVariant,
    onSurfaceVariant = PickoraOnSurfaceVariant,
    outline = PickoraOutline
)

@Composable
fun PickoraTheme(
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
        content = content
    )
}
