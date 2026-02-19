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

        // Убираем верхнюю панель (Action Bar) для красоты
        supportActionBar?.hide()

        // Задержка 2000 миллисекунд (2 секунды)
        Handler(Looper.getMainLooper()).postDelayed({
            // Переход в AuthActivity (где наши фрагменты регистрации)
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)

            // finish() нужен, чтобы пользователь не вернулся на Splash кнопкой "Назад"
            finish()
        }, 2000)
    }
}
