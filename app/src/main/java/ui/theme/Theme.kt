package com.example.kodydomofonowe.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),       // zielony K
    onPrimary = Color.White,
    secondary = Color(0xFFFF9800),     // pomarańczowy D
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.Black,
    secondary = Color(0xFFFF9800),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)

@Composable
fun MyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // możesz dostosować później
        shapes = Shapes(),         // możesz dostosować później
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.background // pełne tło!
            ) {
                content()
            }
        }
    )
}
