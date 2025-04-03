package com.example.kodydomofonowe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🔧 To wywołuje nowy systemowy splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Możesz tutaj poczekać na coś albo od razu startować
        // W naszym przypadku czekamy 2 sekundy – jak dawniej
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // tu możesz dać mniej, np. 500 ms
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}
