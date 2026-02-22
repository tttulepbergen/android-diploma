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

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)

            finish()
        }, 2000)
    }
}
