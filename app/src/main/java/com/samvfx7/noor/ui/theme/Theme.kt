package com.samvfx7.noor.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = MinimalPrimaryDark,
    onPrimary = MinimalOnPrimaryDark,
    primaryContainer = MinimalPrimaryContainerDark,
    onPrimaryContainer = MinimalOnPrimaryContainerDark,
    secondary = MinimalSecondaryDark,
    onSecondary = MinimalOnSecondaryDark,
    secondaryContainer = MinimalSecondaryContainerDark,
    onSecondaryContainer = MinimalOnSecondaryContainerDark,
    tertiary = MinimalTertiaryDark,
    onTertiary = MinimalOnTertiaryDark,
    tertiaryContainer = MinimalTertiaryContainerDark,
    onTertiaryContainer = MinimalOnTertiaryContainerDark,
    background = MinimalBackgroundDark,
    onBackground = MinimalOnBackgroundDark,
    surface = MinimalSurfaceDark,
    onSurface = MinimalOnSurfaceDark,
    surfaceVariant = MinimalSurfaceVariantDark,
    onSurfaceVariant = MinimalOnSurfaceVariantDark,
    outline = MinimalOutlineDark,
    outlineVariant = MinimalOutlineDark.copy(alpha = 0.5f),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MinimalPrimaryLight,
    onPrimary = MinimalOnPrimaryLight,
    primaryContainer = MinimalPrimaryContainerLight,
    onPrimaryContainer = MinimalOnPrimaryContainerLight,
    secondary = MinimalSecondaryLight,
    onSecondary = MinimalOnSecondaryLight,
    secondaryContainer = MinimalSecondaryContainerLight,
    onSecondaryContainer = MinimalOnSecondaryContainerLight,
    tertiary = MinimalTertiaryLight,
    onTertiary = MinimalOnTertiaryLight,
    tertiaryContainer = MinimalTertiaryContainerLight,
    onTertiaryContainer = MinimalOnTertiaryContainerLight,
    background = MinimalBackgroundLight,
    onBackground = MinimalOnBackgroundLight,
    surface = MinimalSurfaceLight,
    onSurface = MinimalOnSurfaceLight,
    surfaceVariant = MinimalSurfaceVariantLight,
    onSurfaceVariant = MinimalOnSurfaceVariantLight,
    outline = MinimalOutlineLight,
    outlineVariant = MinimalOutlineLight.copy(alpha = 0.6f),
  )

@Composable
fun NoorTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve brand palette by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  NoorTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

