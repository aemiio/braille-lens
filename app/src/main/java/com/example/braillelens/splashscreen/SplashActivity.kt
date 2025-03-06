package com.example.braillelens.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.braillelens.MainActivity
import com.example.braillelens.R

class SplashActivity : AppCompatActivity() {

    private val splashDelay = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Optional: Add animations for elements
        // val logo = findViewById<ImageView>(R.id.ivLogo)
        // logo.alpha = 0f
        // logo.animate().alpha(1f).setDuration(1000).start()

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close splash activity
        }, splashDelay)
    }
}