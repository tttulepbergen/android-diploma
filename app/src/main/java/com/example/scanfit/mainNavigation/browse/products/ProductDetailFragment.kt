package com.example.scanfit.mainNavigation.browse.products

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.scanfit.network.AnalysisResponse
import com.example.scanfit.R
import com.example.scanfit.mainNavigation.TrackerViewModel
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.data.FoodItem
import com.example.scanfit.data.RecentProduct
import com.example.scanfit.databinding.FragmentProductDetailBinding
import kotlinx.coroutines.launch
import kotlin.collections.iterator


class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val trackerViewModel: TrackerViewModel by activityViewModels()
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)

        val aiResponse = arguments?.getSerializable("ai_analysis") as? AnalysisResponse

        val foodItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("foodItem", FoodItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("foodItem") as? FoodItem
        }

        if (aiResponse != null) {
            setupAiUI(aiResponse)
        } else if (foodItem != null) {
            setupUI(foodItem)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }



    private fun setupUI(item: FoodItem) {
        binding.tvProductName.text = item.title
        binding.tvCategoryLabel.text = item.subtitle ?: "PRODUCT"
        binding.ivProductImage.load(item.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }

        val p = item.proteins.replace(',', '.').filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
        val c = item.carbs.replace(',', '.').filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
        val f = item.fat.replace(',', '.').filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f

        binding.progressProtein.progress = (p * 2).toInt()
        binding.progressCarbs.progress = (c * 2).toInt()
        binding.progressFat.progress = (f * 2).toInt()

        binding.tvCaloriesValue.text = "${item.calories}\nper serving"

        binding.tvGradeBadge.text = item.grade ?: "B"
        setupGradeColor(item.grade)

        binding.tvProteinValue.text = item.proteins
        binding.tvCarbsValue.text = item.carbs
        binding.tvFatValue.text = item.fat

        setupNutrientsList(item)

        binding.btnAddFood.setOnClickListener {
            trackerViewModel.addFoodData(item)
            Toast.makeText(requireContext(), "${item.title} added!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }


    private fun setupGradeColor(grade: String?) {
        val color = when (grade?.uppercase()) {
            "A" -> "#2E7D32"
            "B" -> "#8BC34A"
            "C" -> "#FBC02D"
            "D" -> "#F57C00"
            "E" -> "#D32F2F"
            else -> "#BDBDBD"
        }
        binding.tvGradeBadge.background?.setTint(Color.parseColor(color))
    }


    private fun setupNutrientsList(item: FoodItem) {
        binding.nutrientsContainer.removeAllViews()

        val nutrientMap = linkedMapOf(
            "Total Fat" to item.fat,
            "Protein" to item.proteins,
            "Total Carbohydrate" to item.carbs,
            "Sugar" to item.sugars,
            "Fiber" to item.fiber,
            "Sodium" to item.sodium,
            "Cholesterol" to item.cholesterol
        )

        for ((name, value) in nutrientMap) {
            val displayValue = if (value.isNullOrBlank()) "0g" else value
            addNutrientRow(name, displayValue)
        }
    }

    private fun addNutrientRow(name: String, value: String) {
        val context = requireContext()

        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val verticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
            setPadding(0, verticalPadding, 0, verticalPadding)
            gravity = Gravity.CENTER_VERTICAL
        }

        val nameTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = name
            setTextColor(Color.parseColor("#1A1C1E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        }

        val valueTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = value
            setPadding(0, 0, 16, 0)
            setTextColor(Color.parseColor("#1A1C1E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
        }

        val checkIcon = ImageView(context).apply {
            val iconSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics).toInt()
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            setImageResource(R.drawable.ic_baby_food) // Замени на свою иконку галочки
            imageTintList = ColorStateList.valueOf(Color.BLACK)
        }

        rowLayout.addView(nameTextView)
        rowLayout.addView(valueTextView)
        rowLayout.addView(checkIcon)

        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
            setBackgroundColor(Color.parseColor("#F1F1F1"))
        }

        binding.nutrientsContainer.addView(rowLayout)
        binding.nutrientsContainer.addView(divider)
    }

    private fun saveToRecent(item: FoodItem) {
        val recentProduct = RecentProduct(
            id = item.title,
            title = item.title,
            subtitle = item.subtitle,
            imageUrl = item.imageUrl,
            calories = item.calories,
            grade = item.grade,
            timestamp = System.currentTimeMillis()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                database.productDao().insertRecent(recentProduct)
            } catch (e: Exception) {
                Log.e("ProductDetail", "Error: ${e.message}")
            }
        }
    }


    private fun setupAiUI(response: AnalysisResponse) {
        binding.tvProductName.text = "AI Анализ"
        binding.tvCategoryLabel.text = "СКАНЕР"

        val (color, grade) = when {
            response.health_score >= 80 -> "#4CAF50" to "A"
            response.health_score >= 60 -> "#8BC34A" to "B"
            response.health_score >= 40 -> "#FBC02D" to "C"
            else -> "#F44336" to "E"
        }

        binding.tvGradeBadge.text = grade
        binding.tvGradeBadge.background?.setTint(Color.parseColor(color))
        binding.tvVerdictStatus.setTextColor(Color.parseColor(color))
        binding.tvVerdictStatus.text = if (response.health_score < 40) "❌ ОПАСНО" else "✅ БЕЗОПАСНО"

        binding.tvAiVerdictDescription.text = response.verdict

        response.macros?.let { m ->
            binding.tvCaloriesValue.text = "${m.calories} kcal\nper portion"
            binding.tvProteinValue.text = "${m.proteins}g"
            binding.tvCarbsValue.text = "${m.carbs}g"
            binding.tvFatValue.text = "${m.fats}g"

            binding.progressProtein.progress = (m.proteins * 2).toInt()
            binding.progressCarbs.progress = (m.carbs * 2).toInt()
            binding.progressFat.progress = (m.fats * 2).toInt()

            binding.progressProtein.visibility = View.VISIBLE
            binding.progressCarbs.visibility = View.VISIBLE
            binding.progressFat.visibility = View.VISIBLE
        }

        setupNutrientsFromAi(response.risks)
    }

    private fun setupNutrientsFromAi(risks: List<String>) {
        binding.nutrientsContainer.removeAllViews()

        if (risks.isEmpty()) {
            addNutrientRow("Health Risks", "None detected ✨")
        } else {
            risks.forEach { risk ->
                addNutrientRow("Risk", risk)
            }
        }

        binding.progressProtein.visibility = View.INVISIBLE
        binding.progressCarbs.visibility = View.INVISIBLE
        binding.progressFat.visibility = View.INVISIBLE
        binding.tvCaloriesValue.text = "AI Scan\nComplete"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }}