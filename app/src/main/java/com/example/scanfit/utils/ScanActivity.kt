package com.example.scanfit.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.ActivityScanBinding
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.network.RegisterDeviceTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var navController: NavController

    // Фрагменты, которые являются «дочерними» для каждой вкладки bottom nav.
    // Когда мы находимся на дочернем фрагменте, подсвечиваем его родительскую вкладку.
    private val scanChildren = setOf(
        R.id.nav_scan,
        R.id.categoriesFragment,
        R.id.subCategoriesFragment,
        R.id.productListFragment,
        R.id.productDetailFragment,
        R.id.searchFragment,
        R.id.compareProductsFragment
    )

    private val trackerChildren = setOf(
        R.id.nav_home,
        R.id.homeFragment,
        R.id.consumptionHistoryFragment,
        R.id.exportReportFragment,
        R.id.userProfileFragment,
        R.id.manualFoodEntryFragment,
        R.id.proSubscriptionFragment
    )

    private val favoritesChildren = setOf(
        R.id.favoritesFragment
    )

    private val recentChildren = setOf(
        R.id.nav_recent
    )

    // Фрагменты без bottom nav (онбординг / авторизация)
    private val hiddenBottomNav = setOf(
        R.id.loginFragment3,
        R.id.forgotPasswordFragment,
        R.id.resetPasswordFragment,
        R.id.signUpFragment,
        R.id.verifyPinFragment,
        R.id.profileFragment2,
        R.id.dietSelectionFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_scan) as NavHostFragment
        navController = navHostFragment.navController

        // setupWithNavController обеспечивает базовую навигацию по клику на иконку.
        // Но для подсветки иконки при "дочерних" фрагментах мы управляем вручную ниже.
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val destId = destination.id

            if (destId in hiddenBottomNav) {
                binding.bottomNavigation.visibility = android.view.View.GONE
                return@addOnDestinationChangedListener
            }

            binding.bottomNavigation.visibility = android.view.View.VISIBLE

            // Вручную выставляем выбранный item в bottom nav
            // чтобы иконка правильно подсвечивалась на дочерних экранах.
            // setOnItemSelectedListener НЕ переопределяем — это сломает навигацию.
            val targetItemId = when (destId) {
                in trackerChildren  -> R.id.nav_home
                in scanChildren     -> R.id.nav_scan
                in favoritesChildren -> R.id.favoritesFragment
                in recentChildren   -> R.id.nav_recent
                else                -> return@addOnDestinationChangedListener
            }

            // Меняем выделенный item без повторного запуска навигации
            if (binding.bottomNavigation.selectedItemId != targetItemId) {
                binding.bottomNavigation.menu.findItem(targetItemId)?.isChecked = true
            }
        }

        setupSystemBars()
        requestNotificationPermission()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) registerFcmToken()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                registerFcmToken()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            registerFcmToken()
        }
    }

    private fun registerFcmToken() {
        val authToken = SessionManager(this).fetchAuthToken() ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            lifecycleScope.launch(Dispatchers.IO) {
                runCatching {
                    NetworkClient.userApiService.registerDeviceToken(
                        "Bearer $authToken",
                        RegisterDeviceTokenRequest(token = fcmToken)
                    )
                }
            }
        }
    }

    private fun setupSystemBars() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }
}
