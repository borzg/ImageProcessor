package com.borzg.imageprocessor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xFFFF5722),
    primaryVariant = Color(0xFFE64A19),
    secondary = Color(0xFF00BCD4),
    onPrimary = Color.White,
    onSecondary = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color(0xFFFF5722),
    primaryVariant = Color(0xFFE64A19),
    secondary = Color(0xFF00BCD4),
    onPrimary = Color.White,
    onSecondary = Color.White
)

@Composable
fun ImageProcessorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}