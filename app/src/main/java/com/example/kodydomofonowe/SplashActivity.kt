package com.example.kodydomofonowe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    var scale by remember { mutableStateOf(1f) }

    // Animacja skalowania
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 1200),
        label = "SplashScale"
    )

    // Przejście do głównej aktywności po zakończeniu animacji + pauza
    LaunchedEffect(Unit) {
        scale = 3.5f            // Powiększenie KD
        delay(1000)             // ⏱️ Czas trwania animacji
        delay(500)             // ⏸️ Pauza z dużym KD – ZMIENIAJ TĘ WARTOŚĆ!
        context.startActivity(Intent(context, MainActivity::class.java))
        (context as? ComponentActivity)?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.scale(animatedScale)
        ) {
            Text(
                text = "K",
                color = Color(0xFF4CAF50), // Zielony
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(0.dp))
            Text(
                text = "D",
                color = Color(0xFFFF9800), // Pomarańczowy
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
