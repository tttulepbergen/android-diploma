package com.example.scanfit.mainNavigation

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.scanfit.mainNavigation.scan.FoodAnalyzer
import com.example.scanfit.network.AnalysisResponse

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

        val currentUser = FirebaseAuth.getInstance().currentUser
        val username = currentUser?.displayName ?: "User"
        binding.tvGreeting.text = "Hi, $username"

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
            binding.tvWaterCount.text = String.format("%.2f L", (count * 0.25))
        }
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
}