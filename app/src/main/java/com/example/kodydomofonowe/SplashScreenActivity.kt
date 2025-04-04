package com.example.kodydomofonowe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreenContent()
        }
    }
}
