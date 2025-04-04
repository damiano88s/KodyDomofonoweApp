package com.example.kodydomofonowe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.tween




class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreenContent()
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // ile czasu splash ma trwać
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current

    var showFullText by remember { mutableStateOf(false) }

    val enterAnim = remember {
        fadeIn(animationSpec = tween(500)) + slideInHorizontally(
            initialOffsetX = { -100 },
            animationSpec = tween(500)
        )
    }

    LaunchedEffect(Unit) {
        delay(1000)
        showFullText = true
        delay(1000)
        context.startActivity(Intent(context, MainActivity::class.java))
        (context as? ComponentActivity)?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.padding(start = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Górna linia: K + ODY
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "K",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (showFullText) {
                    AnimatedVisibility(
                        visible = true,
                        enter = enterAnim
                    ) {
                        Text(
                            text = "ODY",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF4CAF50),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dolna linia: D + OMOFONOWE
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "D",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (showFullText) {
                    AnimatedVisibility(
                        visible = true,
                        enter = enterAnim
                    ) {
                        Text(
                            text = "OMOFONOWE",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFFF9800),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}


