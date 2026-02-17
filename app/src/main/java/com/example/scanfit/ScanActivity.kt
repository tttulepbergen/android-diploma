package com.example.scanfit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.scanfit.databinding.ActivityScanBinding

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Инициализация Binding
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Настройка Navigation Host (контейнера для фрагментов)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_scan) as NavHostFragment
        navController = navHostFragment.navController

        // 3. Привязка BottomNavigationView к навигации
        // Теперь при нажатии на иконки в меню, NavController будет сам менять фрагменты
        binding.bottomNavigation.setupWithNavController(navController)

        // Убираем лишние системные бары для полноэкранного режима (по желанию)
        setupSystemBars()
    }

    private fun setupSystemBars() {
        // Здесь можно оставить твой код для прозрачного статус-бара,
        // чтобы камера во фрагменте смотрелась красиво
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }
}