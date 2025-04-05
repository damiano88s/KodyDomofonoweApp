package com.example.kodydomofonowe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import com.example.kodydomofonowe.ui.theme.MyTheme
import androidx.compose.foundation.isSystemInDarkTheme




class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sharedPreferences = getSharedPreferences("appPreferences", MODE_PRIVATE)
            val isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false)

            setContent {
                MyTheme(darkTheme = isDarkTheme) {
                    SplashScreenContent {
                        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }

        }


    }
}

@Composable
fun SplashScreenContent(onAnimationFinished: () -> Unit) {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }

    val targetScale = 2.5f // siła powiększenia
    val animationDuration = 1000 // czas trwania animacji w ms
    val pauseAfterAnimation = 0 // pauza po animacji w ms

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) targetScale else 1f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = LinearOutSlowInEasing
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(animationDuration.toLong()) // czekamy na koniec animacji
        delay(pauseAfterAnimation.toLong()) // potem dodatkowa pauza
        onAnimationFinished()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // 👈 to dodaje tło zgodne z motywem
        contentAlignment = Alignment.Center
    ) {

    Image(
            painter = painterResource(id = R.drawable.logo_kd),
            contentDescription = "Logo KD",
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
        )
    }
}
