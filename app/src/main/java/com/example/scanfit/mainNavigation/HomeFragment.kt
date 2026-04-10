package com.example.scanfit.mainNavigation

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentHomeBinding
import com.example.scanfit.model.UserCaloriesDaily
import com.example.scanfit.model.UserCaloriesData
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.example.scanfit.network.NetworkClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.scanfit.mainNavigation.scan.FoodAnalyzer
import com.example.scanfit.network.AnalysisResponse
import com.example.scanfit.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val trackerViewModel: TrackerViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager
    private var currentCalorieGoal = 2150
    private var currentProteinGoal = 150f
    private var currentFatGoal = 70f
    private var currentCarbGoal = 300f

    private val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setGalleryImportAllowed(true)
        .build()

    private val scanner = GmsDocumentScanning.getClient(scannerOptions)

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pages?.get(0)?.imageUri?.let { uri ->
                processScannedImage(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        val currentUser = FirebaseAuth.getInstance().currentUser
        val username = currentUser?.displayName ?: "User"
        binding.tvGreeting.text = "Hi, $username"

        setupTrackerObserver()

        trackerViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            updateCalendarUI(date)
            fetchCaloriesForDate(date)
        }

        binding.cvProfileIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_userProfileFragment)
        }

        setupScannerTrigger()

        binding.cardCalories.setOnClickListener {
            showNutrientDetails()
        }

        binding.tvGreeting.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = trackerViewModel.selectedDate.value ?: Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(year, month, dayOfMonth)
                trackerViewModel.setSelectedDate(newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateCalendarUI(selectedCalendar: Calendar) {
        binding.layoutCalendar.removeAllViews()
        val calendar = selectedCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfNum = SimpleDateFormat("dd", Locale.getDefault())

        for (i in 0 until 7) {
            val dateForView = calendar.clone() as Calendar
            val isSelected = isSameDay(dateForView, selectedCalendar)

            val dayView = createDayView(
                dayName = sdfDay.format(dateForView.time),
                dayNum = sdfNum.format(dateForView.time),
                isSelected = isSelected
            )

            dayView.setOnClickListener {
                trackerViewModel.setSelectedDate(dateForView)
            }
            binding.layoutCalendar.addView(dayView)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun createDayView(dayName: String, dayNum: String, isSelected: Boolean): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(65), 1f).apply {
                setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }
            if (isSelected) {
                setBackgroundResource(R.drawable.bg_selected_day)
                elevation = dpToPx(2).toFloat()
            }
        }

        val tvName = TextView(requireContext()).apply {
            text = dayName
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.GRAY)
        }

        val tvNum = TextView(requireContext()).apply {
            text = dayNum
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.GRAY)
        }

        layout.addView(tvName)
        layout.addView(tvNum)
        return layout
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun setupTrackerObserver() {
        trackerViewModel.totalCalories.observe(viewLifecycleOwner) { total ->
            binding.tvCaloriesCount.text = "$total Cal"
            val left = (currentCalorieGoal - total).coerceAtLeast(0)
            binding.tvCaloriesLeft.text = "$left Cal left"
            binding.calorieProgressBar.progress = calculateProgress(total.toFloat(), currentCalorieGoal.toFloat())
        }

        trackerViewModel.totalProteins.observe(viewLifecycleOwner) { total ->
            binding.tvProteinsMain.text = "${total.toInt()} g"
            val left = currentProteinGoal.toInt() - total.toInt()
            binding.tvProteinsLeft.text = "${if (left > 0) left else 0} g left"
            binding.progressProteins.progress = calculateProgress(total.toFloat(), currentProteinGoal)
        }

        trackerViewModel.totalFat.observe(viewLifecycleOwner) { total ->
            binding.tvFatMain.text = "${total.toInt()} g"
            val left = currentFatGoal.toInt() - total.toInt()
            binding.tvFatLeft.text = "${if (left > 0) left else 0} g left"
            binding.progressFat.progress = calculateProgress(total.toFloat(), currentFatGoal)
        }

        trackerViewModel.totalCarbs.observe(viewLifecycleOwner) { total ->
            binding.tvCarbsMain.text = "${total.toInt()} g"
            val left = currentCarbGoal.toInt() - total.toInt()
            binding.tvCarbsLeft.text = "${if (left > 0) left else 0} g left"
            binding.progressCarbs.progress = calculateProgress(total.toFloat(), currentCarbGoal)
        }

        trackerViewModel.goalCalories.observe(viewLifecycleOwner) {
            currentCalorieGoal = it
            binding.tvCaloriesLeft.text =
                "${(it - (trackerViewModel.totalCalories.value ?: 0)).coerceAtLeast(0)} Cal left"
            binding.calorieProgressBar.progress =
                calculateProgress((trackerViewModel.totalCalories.value ?: 0).toFloat(), it.toFloat())
        }

        trackerViewModel.goalProteins.observe(viewLifecycleOwner) {
            currentProteinGoal = it
            val total = trackerViewModel.totalProteins.value ?: 0f
            binding.tvProteinsLeft.text = "${(it.toInt() - total.toInt()).coerceAtLeast(0)} g left"
            binding.progressProteins.progress = calculateProgress(total, it)
        }

        trackerViewModel.goalFat.observe(viewLifecycleOwner) {
            currentFatGoal = it
            val total = trackerViewModel.totalFat.value ?: 0f
            binding.tvFatLeft.text = "${(it.toInt() - total.toInt()).coerceAtLeast(0)} g left"
            binding.progressFat.progress = calculateProgress(total, it)
        }

        trackerViewModel.goalCarbs.observe(viewLifecycleOwner) {
            currentCarbGoal = it
            val total = trackerViewModel.totalCarbs.value ?: 0f
            binding.tvCarbsLeft.text = "${(it.toInt() - total.toInt()).coerceAtLeast(0)} g left"
            binding.progressCarbs.progress = calculateProgress(total, it)
        }

        trackerViewModel.waterGlasses.observe(viewLifecycleOwner) { count ->
            renderWaterGlasses(count)
            binding.tvWaterCount.text = String.format("%.2f L", (count * 0.25))
        }
    }

    private fun fetchCaloriesForDate(selectedDate: Calendar) {
        val token = sessionManager.fetchAuthToken() ?: return
        val day = API_DATE_FORMAT.format(selectedDate.time)

        lifecycleScope.launch {
            try {
                val response = if (isSameDay(selectedDate, Calendar.getInstance())) {
                    sessionManager.refreshUserRole()
                    NetworkClient.userApiService.getTodayUserCalories(token)
                } else {
                    sessionManager.refreshUserRole()
                    NetworkClient.userApiService.getUserCaloriesByDay(token, day)
                }

                if (response.success) {
                    applyCaloriesData(response.data)
                } else {
                    applyCaloriesData(null)
                }
            } catch (_: Exception) {
                applyCaloriesData(null)
            }
        }
    }

    private fun applyCaloriesData(data: UserCaloriesData?) {
        applyDailyGoals(data)
        trackerViewModel.setNutritionTotals(
            calories = data?.daily?.calories ?: 0,
            proteins = (data?.daily?.proteins ?: 0).toFloat(),
            fat = (data?.daily?.fat ?: 0).toFloat(),
            carbs = (data?.daily?.carbs ?: 0).toFloat()
        )
    }

    private fun applyDailyGoals(data: UserCaloriesData?) {
        trackerViewModel.setNutritionGoals(
            calories = data?.calories,
            proteins = data?.proteins?.toFloat(),
            fat = data?.fat?.toFloat(),
            carbs = data?.carbs?.toFloat()
        )
    }

    private fun showNutrientDetails() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDate = trackerViewModel.selectedDate.value ?: Calendar.getInstance()
        val day = API_DATE_FORMAT.format(selectedDate.time)

        lifecycleScope.launch {
            try {
                sessionManager.refreshUserRole()
                val response = NetworkClient.userApiService.getUserCaloriesByDay(token, day)
                if (response.success) {
                    showNutrientDetailsSheet(day, response.data)
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "Failed to load nutrients",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Failed to load nutrients",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showNutrientDetailsSheet(day: String, data: UserCaloriesData?) {
        val dialog = BottomSheetDialog(requireContext())
        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(20), dpToPx(24), dpToPx(28))
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(
                    dpToPx(24).toFloat(), dpToPx(24).toFloat(),
                    dpToPx(24).toFloat(), dpToPx(24).toFloat(),
                    0f, 0f,
                    0f, 0f
                )
                setColor(Color.WHITE)
            }
        }

        content.addView(TextView(requireContext()).apply {
            text = "Nutrients"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#1A1C1E"))
        })

        content.addView(TextView(requireContext()).apply {
            text = day
            textSize = 14f
            setTextColor(Color.parseColor("#808080"))
            setPadding(0, dpToPx(4), 0, dpToPx(18))
        })

        val daily = data?.daily
        val nutrients = listOf(
            NutrientDetail("Calories", daily?.calories, data?.calories, "kcal"),
            NutrientDetail("Carbs", daily?.carbs, data?.carbs, "g"),
            NutrientDetail("Protein", daily?.proteins, data?.proteins, "g"),
            NutrientDetail("Fats", daily?.fat, data?.fat, "g"),
            NutrientDetail("Sodium", daily?.sodium, data?.sodium, "mg", isPremium = true),
            NutrientDetail("Fiber", daily?.fiber, data?.fiber, "g", isPremium = true),
            NutrientDetail("Sugar", daily?.sugar, data?.sugar, "g", isPremium = true),
            NutrientDetail("Cholesterol", daily?.cholesterol, data?.cholesterol, "mg", isPremium = true)
        )

        nutrients.forEachIndexed { index, item ->
            content.addView(createNutrientDetailRow(item))
            if (index != nutrients.lastIndex) {
                content.addView(createNutrientDivider())
            }
        }

        dialog.setContentView(content)
        dialog.show()
    }

    private fun createNutrientDetailRow(item: NutrientDetail): View {
        val isLocked = item.isPremium && !sessionManager.isVip()
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dpToPx(14), 0, dpToPx(14))
            alpha = if (isLocked) 0.72f else 1.0f

            val labelView = TextView(context).apply {
                text = item.name
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor(if (isLocked) "#6B7280" else "#1A1C1E"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val valueView = TextView(context).apply {
                text = if (isLocked) "ScanFit Pro" else formatNutrientValue(item)
                textSize = if (isLocked) 13f else 15f
                typeface = if (isLocked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(Color.parseColor(if (isLocked) "#2F6BFF" else "#4B5563"))
                if (isLocked) {
                    setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5))
                    background = android.graphics.drawable.GradientDrawable().apply {
                        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                        cornerRadius = dpToPx(50).toFloat()
                        setColor(Color.parseColor("#E8F0FF"))
                    }
                }
            }

            addView(labelView)
            addView(valueView)
        }
    }

    private fun createNutrientDivider(): View {
        return View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
            )
            setBackgroundColor(Color.parseColor("#EDF2F7"))
        }
    }

    private fun formatNutrientValue(item: NutrientDetail): String {
        val consumed = item.consumed ?: 0
        val goal = item.goal ?: 0
        return "$consumed / $goal ${item.unit}"
    }

    private fun calculateProgress(value: Float, limit: Float): Int {
        if (limit <= 0f) return 0
        return ((value / limit) * 100).toInt().coerceIn(0, 100)
    }



    private fun renderWaterGlasses(count: Int) {
        binding.waterStack.removeAllViews()
        val maxGlasses = 7
        val glassHeight = 75
        for (i in 0 until maxGlasses) {
            val imageView = ImageView(requireContext())

            val params = LinearLayout.LayoutParams(0, dpToPx(glassHeight), 1f).apply {
                setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }

            imageView.layoutParams = params

            imageView.scaleType = ImageView.ScaleType.FIT_CENTER

            when {
                i < count -> {
                    imageView.setImageResource(R.drawable.ic_glass_full)
                    imageView.setOnClickListener { trackerViewModel.removeWaterGlass() }
                }
                i == count -> {
                    imageView.setImageResource(R.drawable.ic_glass_add)
                    imageView.setOnClickListener { trackerViewModel.addWaterGlass() }
                }
                else -> {
                    imageView.setImageResource(R.drawable.ic_glass_empty)
                }
            }
            binding.waterStack.addView(imageView)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun setupScannerTrigger() { /* Логика вызова сканера */ }

    private fun processScannedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val prefs = requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
                val diseasesSet = prefs.getStringSet("user_diseases", emptySet())
                val healthInfo = diseasesSet?.joinToString(", ") ?: "Ограничений нет"
                val bitmap = uriToBitmap(uri)
                val aiResponse = arguments?.getSerializable("ai_analysis") as? AnalysisResponse
                showAnalysisResult(aiResponse)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Analysis failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAnalysisResult(result: AnalysisResponse?) {

        if (result == null) {
            Toast.makeText(requireContext(), "Analysis failed", Toast.LENGTH_SHORT).show()
            return
        }

        val bundle = Bundle().apply {
            putSerializable("ai_analysis", result)
        }

        findNavController().navigate(
            R.id.productDetailFragment,
            bundle
        )
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE }
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
        val scale = 1024f / Math.max(bitmap.width, bitmap.height)
        return if (scale < 1) Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true) else bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    private data class NutrientDetail(
        val name: String,
        val consumed: Int?,
        val goal: Int?,
        val unit: String,
        val isPremium: Boolean = false
    )
}
