package com.example.scanfit

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.scanfit.analyzer.FoodAnalyzer
import com.example.scanfit.databinding.FragmentHomeBinding
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val trackerViewModel: TrackerViewModel by activityViewModels()

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

        binding.tvGreeting.text = "Hi, Anel"

        setupTrackerObserver()

        trackerViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            updateCalendarUI(date)
        }

        binding.cvProfileIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_userProfileFragment)
        }

        setupScannerTrigger()

        binding.tvGreeting.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = trackerViewModel.selectedDate.value ?: java.util.Calendar.getInstance()

        android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newDate = java.util.Calendar.getInstance()
                newDate.set(year, month, dayOfMonth)
                trackerViewModel.setSelectedDate(newDate)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateCalendarUI(selectedCalendar: java.util.Calendar) {
        binding.layoutCalendar.removeAllViews()

        val calendar = selectedCalendar.clone() as java.util.Calendar
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)

        val sdfDay = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val sdfNum = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault())

        for (i in 0 until 7) {
            val dateForView = calendar.clone() as java.util.Calendar
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
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun createDayView(dayName: String, dayNum: String, isSelected: Boolean): View {
        // Если выбран — белый фон и CardView, если нет — просто текст
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
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
            gravity = android.view.Gravity.CENTER
            setTextColor(if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.GRAY)
        }

        val tvNum = TextView(requireContext()).apply {
            text = dayNum
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setTextColor(if (isSelected) android.graphics.Color.BLACK else android.graphics.Color.GRAY)
        }

        layout.addView(tvName)
        layout.addView(tvNum)
        return layout
    }

    private fun isSameDay(cal1: java.util.Calendar, cal2: java.util.Calendar): Boolean {
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }




    private fun setupTrackerObserver() {
        trackerViewModel.totalCalories.observe(viewLifecycleOwner) { total ->
            binding.tvCaloriesCount.text = "$total Cal"
            val limit = 2150
            val left = limit - total
            binding.tvCaloriesLeft.text = "$left Cal left"
            binding.calorieProgressBar.progress = (total.toFloat() / limit * 100).toInt()
        }

        trackerViewModel.totalProteins.observe(viewLifecycleOwner) { total ->
            val limit = 150
            binding.tvProteinsMain.text = "${total.toInt()} g"

            val left = limit - total.toInt()
            binding.tvProteinsLeft.text = "${if (left > 0) left else 0} g left"

            binding.progressProteins.progress = (total.toFloat() / limit * 100).toInt()
        }

        trackerViewModel.totalFat.observe(viewLifecycleOwner) { total ->
            val limit = 70
            binding.tvFatMain.text = "${total.toInt()} g"

            val left = limit - total.toInt()
            binding.tvFatLeft.text = "${if (left > 0) left else 0} g left"

            binding.progressFat.progress = (total.toFloat() / limit * 100).toInt()
        }

        trackerViewModel.totalCarbs.observe(viewLifecycleOwner) { total ->
            val limit = 300
            binding.tvCarbsMain.text = "${total.toInt()} g"

            val left = limit - total.toInt()
            binding.tvCarbsLeft.text = "${if (left > 0) left else 0} g left"

            binding.progressCarbs.progress = (total.toFloat() / limit * 100).toInt()
        }

        trackerViewModel.waterGlasses.observe(viewLifecycleOwner) { count ->
            renderWaterGlasses(count)
            binding.tvWaterCount.text = "${(count * 0.25)} L"
        }
    }





    private fun renderWaterGlasses(count: Int) {
        binding.waterStack.removeAllViews()

        val maxGlasses = 7
        val glassSize = 40
        val glassCapacity = 0.25
        val dailyGoal = 1.74

        binding.waterStack.weightSum = maxGlasses.toFloat()

        for (i in 0 until maxGlasses) {
            val imageView = ImageView(requireContext())

            val params = LinearLayout.LayoutParams(
                0,
                dpToPx(glassSize),
                1f
            ).apply {
                setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }
            imageView.layoutParams = params

            when {
                i < count -> {
                    imageView.setImageResource(R.drawable.ic_glass_full)
                    imageView.setOnClickListener {
                        trackerViewModel.removeWaterGlass()
                    }
                }
                i == count -> {
                    imageView.setImageResource(R.drawable.ic_glass_add)
                    imageView.setOnClickListener {
                        trackerViewModel.addWaterGlass()
                    }
                }
                else -> {
                    imageView.setImageResource(R.drawable.ic_glass_empty)
                }
            }
            binding.waterStack.addView(imageView)
        }

        val consumed = count * glassCapacity
        val remaining = dailyGoal - consumed

        binding.tvWaterGoal.text = if (remaining > 0) {
            String.format("Goal %.2f L left", remaining)
        } else {
            "Goal reached! 🎉"
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupScannerTrigger() {
    }

    private fun startScanning() {
        scanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Scanner error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun processScannedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val prefs = requireContext().getSharedPreferences("user_settings", android.content.Context.MODE_PRIVATE)
                val diseasesSet = prefs.getStringSet("user_diseases", emptySet())
                val healthInfo = diseasesSet?.joinToString(", ") ?: "Ограничений нет"

                Toast.makeText(requireContext(), "AI is analyzing for: $healthInfo", Toast.LENGTH_SHORT).show()

                val bitmap = uriToBitmap(uri)

                val aiResponse = FoodAnalyzer.analyzeIngredients(bitmap, healthInfo)

                showAnalysisResult(aiResponse)
            } catch (e: Exception) {
                Log.e("SCAN_DEBUG", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Analysis failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAnalysisResult(result: String) {
        Log.d("SCAN_DEBUG", "AI Result: $result")
        Toast.makeText(requireContext(), "AI: $result", Toast.LENGTH_LONG).show()
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }

        val scale = 1024f / Math.max(bitmap.width, bitmap.height)
        return if (scale < 1) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}