package com.example.scanfit.utils

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import com.example.scanfit.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        val sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ScanActivity::class.java)
            // The ScanActivity has a NavHostFragment that handles the start destination.
            // Usually, the navigation graph start destination would be Login if not authenticated.
            // Or we can pass an extra to ScanActivity.
            startActivity(intent)
            finish()
        }, 2000)
    }
}
