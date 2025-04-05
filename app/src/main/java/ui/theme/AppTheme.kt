package com.example.kodydomofonowe.theme.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography


private val DarkColors = darkColorScheme(
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,

)


private val LightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF2F2F2),
    onSurface = Color.Black
)

@Composable
fun AppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

