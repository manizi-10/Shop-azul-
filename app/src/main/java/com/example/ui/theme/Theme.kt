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

private val DarkColorScheme =
  darkColorScheme(
    primary = ShopAzulPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = ShopAzulPrimaryDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = ShopAzulSecondary,
    tertiary = ShopAzulTertiary,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ShopAzulPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = ShopAzulPrimaryDark,
    secondary = ShopAzulSecondary,
    tertiary = ShopAzulTertiary,
    background = ShopAzulBackground,
    onBackground = ShopAzulTextPrimary,
    surface = ShopAzulSurface,
    onSurface = ShopAzulTextPrimary,
    surfaceVariant = ShopAzulSurfaceVariant,
    onSurfaceVariant = ShopAzulTextSecondary,
    outline = ShopAzulBorder
  )

@Composable
fun ShopAzulTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Keep consistent brand blue
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
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit
) {
  ShopAzulTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
