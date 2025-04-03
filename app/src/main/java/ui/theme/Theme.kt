// UWAGA: Nie usuwać tego pliku. Motyw aplikacji działa przez MaterialTheme w Compose.
// Używamy Compose, a nie XML Layoutów. styles.xml nie jest głównym źródłem motywu.


package com.example.kodydomofonowe.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

@Composable
fun MyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}


