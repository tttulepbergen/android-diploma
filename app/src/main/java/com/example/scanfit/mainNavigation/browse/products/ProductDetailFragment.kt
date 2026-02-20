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
import com.example.scanfit.R
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.data.FavoriteProduct
import com.example.scanfit.data.FoodItem
import com.example.scanfit.data.RecentProduct
import com.example.scanfit.databinding.FragmentProductDetailBinding
import com.example.scanfit.mainNavigation.TrackerViewModel
import com.example.scanfit.network.AnalysisResponse
import kotlinx.coroutines.launch
import com.example.scanfit.mainNavigation.scan.FoodAnalyzer

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val trackerViewModel: TrackerViewModel by activityViewModels()
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)

        val aiResponse = arguments?.getSerializable("ai_analysis") as? AnalysisResponse

        val foodItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("foodItem", FoodItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("foodItem") as? FoodItem
        }

        if (aiResponse != null) {
            setupAiUI(aiResponse)
        } else if (foodItem != null) {
            setupUI(foodItem)
            saveToRecent(foodItem)
            startAiAnalysis(foodItem)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }


    private fun startAiAnalysis(item: FoodItem) {
        val queryText = if (item.ingredients.isNullOrBlank()) {
            Log.d("AI_DEBUG", "No ingredients for ${item.title}, using title as fallback.")
            "Product: ${item.title}. Ingredients unknown, please analyze based on general knowledge of this product."
        } else {
            Log.d("AI_DEBUG", "Analyzing ingredients for: ${item.title}")
            item.ingredients
        }

        binding.aiProgressBar.visibility = View.VISIBLE
        binding.tvAiVerdictDescription.text = "AI is analyzing..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = FoodAnalyzer.analyzeTextIngredientsFull(queryText, "General Analysis")

                Log.d("AI_DEBUG", "AI Response Success: ${response.verdict}")

                binding.aiProgressBar.visibility = View.GONE

                handleProductType(response.product_type)

                setupAiUI(response)

            } catch (e: Exception) {
                Log.e("AI_DEBUG", "AI Analysis FAILED: ${e.message}")
                e.printStackTrace()

                binding.aiProgressBar.visibility = View.GONE
                binding.tvAiVerdictDescription.text = "AI Analysis unavailable. Showing standard info."

            }
        }
    }

    private fun handleProductType(type: String?) {
        if (type == "pharmacy") {
            binding.macrosContainer.visibility = View.GONE
            binding.tvVerdictStatus.text = "💊 Pharmacy Info"
            binding.tvVerdictStatus.setTextColor(Color.BLUE)
        } else {
            binding.macrosContainer.visibility = View.VISIBLE
        }
    }

    private fun setupUI(item: FoodItem) {
        binding.tvProductName.text = item.title
        binding.tvCategoryLabel.text = item.subtitle ?: "PRODUCT"
        binding.ivProductImage.load(item.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val existing = database.productDao().getFavoriteById(item.title)
            item.isFavorite = existing != null
            updateFavoriteIcon(item.isFavorite)
        }

        binding.ivFavorite.setOnClickListener {
            item.isFavorite = !item.isFavorite
            updateFavoriteIcon(item.isFavorite)
            handleFavoriteAction(item)
        }

        // Парсинг макросов
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            text = value
            setPadding(0, 0, 16, 0)
            setTextColor(Color.parseColor("#1A1C1E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
        }

        val checkIcon = ImageView(context).apply {
            val iconSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics).toInt()
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            setImageResource(R.drawable.ic_baby_food)
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
                Log.e("ProductDetail", "Error saving to recent: ${e.message}")
            }
        }
    }

    private fun setupAiUI(response: AnalysisResponse) {
        binding.tvProductName.text = "AI Analysis"
        binding.tvCategoryLabel.text = "SCANNER"

        // Добавляем ?: 0, чтобы безопасно сравнивать
        val score = response.health_score ?: 0

        val (color, grade) = when {
            score >= 80 -> "#4CAF50" to "A"
            score >= 60 -> "#8BC34A" to "B"
            score >= 40 -> "#FBC02D" to "C"
            else -> "#F44336" to "E"
        }

        binding.tvGradeBadge.text = grade
        binding.tvGradeBadge.background?.setTint(Color.parseColor(color))
        binding.tvVerdictStatus.setTextColor(Color.parseColor(color))

        // Здесь тоже используем score
        binding.tvVerdictStatus.text = if (score < 40) "❌ Dangerous" else "✅ Safe"
        binding.tvAiVerdictDescription.text = response.verdict ?: "No description"

        response.macros?.let { m ->
            // Добавляем ?: 0.0 для макросов, так как они теперь Double?
            val cal = m.calories ?: 0.0
            val prot = m.proteins ?: 0.0
            val carb = m.carbs ?: 0.0
            val fat = m.fats ?: 0.0

            binding.tvCaloriesValue.text = "${cal} kcal\nper portion"
            binding.tvProteinValue.text = "${prot}g"
            binding.tvCarbsValue.text = "${carb}g"
            binding.tvFatValue.text = "${fat}g"

            binding.progressProtein.progress = (prot * 2).toInt()
            binding.progressCarbs.progress = (carb * 2).toInt()
            binding.progressFat.progress = (fat * 2).toInt()
        }

        setupNutrientsFromAi(response.risks ?: emptyList())
    }

    private fun setupNutrientsFromAi(risks: List<String>) {
        binding.nutrientsContainer.removeAllViews()
        if (risks.isEmpty()) {
            addNutrientRow("Health Risks", "None detected ✨")
        } else {
            risks.forEach { risk -> addNutrientRow("Risk", risk) }
        }
        binding.progressProtein.visibility = View.INVISIBLE
        binding.progressCarbs.visibility = View.INVISIBLE
        binding.progressFat.visibility = View.INVISIBLE
        binding.tvCaloriesValue.text = "AI Scan\nComplete"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val iconRes = if (isFavorite) {
            R.drawable.ic_favourites_liked
        } else {
            R.drawable.ic_favourites
        }

        binding.ivFavorite.setImageResource(iconRes)

        val color = if (isFavorite) "#FF4B4B" else "#BDBDBD"
        binding.ivFavorite.setColorFilter(Color.parseColor(color))
    }
    private fun handleFavoriteAction(item: FoodItem) {
        viewLifecycleOwner.lifecycleScope.launch {

            if (item.isFavorite) {

                val entity = FavoriteProduct(
                    id = item.title,
                    productName = item.title,
                    name = item.title,
                    imageUrl = item.imageUrl,
                    calories = item.calories,
                    grade = item.grade,
                    ingredients = item.ingredients
                )

                database.productDao().insertFavorite(entity)
                Toast.makeText(requireContext(), "Saved to favourites", Toast.LENGTH_SHORT).show()

            } else {

                database.productDao().deleteFavoriteById(item.title)
                Toast.makeText(requireContext(), "Removed from favourites", Toast.LENGTH_SHORT).show()
            }
        }
    }
}