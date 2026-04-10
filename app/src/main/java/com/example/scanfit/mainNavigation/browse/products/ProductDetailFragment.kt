package com.example.scanfit.mainNavigation.browse.products

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.scanfit.model.CreateUserDailyEatRequest
import com.example.scanfit.model.UpdateUserCaloriesRequest
import com.example.scanfit.model.UserCaloriesData
import com.example.scanfit.network.AnalysisResponse
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.launch
import com.example.scanfit.mainNavigation.scan.FoodAnalyzer
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val trackerViewModel: TrackerViewModel by activityViewModels()
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var servingMultiplier = 1.0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)
        sessionManager = SessionManager(requireContext())

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
            binding.tvVerdictStatus.text = "Pharmacy Info"
            binding.tvVerdictStatus.setTextColor(Color.BLUE)
        } else {
            binding.macrosContainer.visibility = View.VISIBLE
        }
    }

    private fun setupUI(item: FoodItem) {
        binding.portionSelectorCard.visibility = View.VISIBLE
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
        setupServingSelector(item)

        binding.btnAddFood.setOnClickListener {
            updateUserCalories(item)
        }
    }

    private fun updateUserCalories(item: FoodItem) {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDate = trackerViewModel.selectedDate.value ?: java.util.Calendar.getInstance()
        val day = API_DATE_FORMAT.format(selectedDate.time)
        val calories = item.calories.toScaledIntValue()
        val carbs = item.carbs.toScaledIntValue()
        val fat = item.fat.toScaledIntValue()
        val proteins = item.proteins.toScaledIntValue()
        val fiber = item.fiber.toScaledIntValue()
        val sodium = item.sodium.toScaledIntValue()
        val sugar = item.sugars.toScaledIntValue()
        val cholesterol = item.cholesterol.toScaledIntValue()
        val vitaminA = item.vitaminA.toScaledDoubleValue()
        val vitaminB12 = item.vitaminB12.toScaledDoubleValue()
        val vitaminB6 = item.vitaminB6.toScaledDoubleValue()
        val vitaminB9 = item.vitaminB9.toScaledDoubleValue()
        val vitaminC = item.vitaminC.toScaledDoubleValue()
        val vitaminD = item.vitaminD.toScaledDoubleValue()
        val vitaminE = item.vitaminE.toScaledDoubleValue()

        val dailyEatRequest = CreateUserDailyEatRequest(
            calorie = calories,
            carbohydrate = carbs,
            cholesterol = cholesterol,
            fats = fat,
            fiber = fiber,
            portion = servingMultiplier,
            productName = item.title,
            protein = proteins,
            sodium = sodium,
            sugar = sugar,
            vitaminA = vitaminA,
            vitaminB12 = vitaminB12,
            vitaminB6 = vitaminB6,
            vitaminB9 = vitaminB9,
            vitaminC = vitaminC,
            vitaminD = vitaminD,
            vitaminE = vitaminE
        )

        val caloriesRequest = UpdateUserCaloriesRequest(
            calories = calories,
            carbs = carbs,
            fat = fat,
            proteins = proteins,
            fiber = fiber,
            sodium = sodium,
            sugar = sugar,
            cholesterol = cholesterol,
            vitaminA = vitaminA,
            vitaminB12 = vitaminB12,
            vitaminB6 = vitaminB6,
            vitaminB9 = vitaminB9,
            vitaminC = vitaminC,
            vitaminD = vitaminD,
            vitaminE = vitaminE
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                sessionManager.refreshUserRole()
                val createResponse = NetworkClient.userApiService.createUserDailyEat(token, dailyEatRequest)
                Log.d("ADD_FOOD", "createUserDailyEat success=${createResponse.success}, message=${createResponse.message}")
                if (createResponse.success == false) {
                    Toast.makeText(
                        requireContext(),
                        createResponse.message ?: "Failed to add food",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val caloriesResponse = NetworkClient.userApiService.updateUserCalories(token, day, caloriesRequest)
                Log.d("ADD_FOOD", "updateUserCalories success=${caloriesResponse.success}, message=${caloriesResponse.message}")
                if (caloriesResponse.success && caloriesResponse.data != null) {
                    applyUpdatedCalories(caloriesResponse.data)
                    Toast.makeText(requireContext(), "${item.title} added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        caloriesResponse.message ?: "Failed to update calories",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("ADD_FOOD", "Add food failed", e)
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Failed to update calories",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyUpdatedCalories(data: UserCaloriesData) {
        trackerViewModel.setNutritionGoals(
            calories = data.calories,
            proteins = data.proteins?.toFloat(),
            fat = data.fat?.toFloat(),
            carbs = data.carbs?.toFloat()
        )
        trackerViewModel.setNutritionTotals(
            calories = data.daily?.calories ?: 0,
            proteins = (data.daily?.proteins ?: 0).toFloat(),
            fat = (data.daily?.fat ?: 0).toFloat(),
            carbs = (data.daily?.carbs ?: 0).toFloat()
        )
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
            "Total Fat" to item.fat.scaledDisplayValue(),
            "Protein" to item.proteins.scaledDisplayValue(),
            "Total Carbohydrate" to item.carbs.scaledDisplayValue(),
            "Sugar" to item.sugars.scaledDisplayValue(),
            "Fiber" to item.fiber.scaledDisplayValue(),
            "Sodium" to item.sodium.scaledDisplayValue(),
            "Cholesterol" to item.cholesterol.scaledDisplayValue(),
            "Vitamin D" to item.vitaminD.scaledDisplayValue(),
            "Vitamin B12" to item.vitaminB12.scaledDisplayValue(),
            "Vitamin C" to item.vitaminC.scaledDisplayValue(),
            "Vitamin A" to item.vitaminA.scaledDisplayValue(),
            "Vitamin B6" to item.vitaminB6.scaledDisplayValue(),
            "Vitamin B9 (Folic acid)" to item.vitaminB9.scaledDisplayValue(),
            "Vitamin E" to item.vitaminE.scaledDisplayValue()
        )

        for ((name, value) in nutrientMap) {
            val displayValue = if (value.isNullOrBlank()) "0g" else value
            addNutrientRow(name, displayValue)
        }
    }

    private fun setupServingSelector(item: FoodItem) {
        val portions = linkedMapOf(
            binding.btnPortionQuarter to 0.25,
            binding.btnPortionHalf to 0.5,
            binding.btnPortionThreeQuarters to 0.75,
            binding.btnPortionOne to 1.0,
            binding.btnPortionOneHalf to 1.5,
            binding.btnPortionTwo to 2.0,
            binding.btnPortionTwoHalf to 2.5,
            binding.btnPortionThree to 3.0
        )

        portions.forEach { (view, value) ->
            view.setOnClickListener {
                servingMultiplier = value
                binding.etPortionAmount.setText(formatAmount(value))
                binding.etPortionAmount.setSelection(binding.etPortionAmount.text?.length ?: 0)
                refreshServingUi(item, portions)
            }
        }

        binding.etPortionAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                servingMultiplier = s.toString().replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                refreshServingUi(item, portions)
            }
        })

        refreshServingUi(item, portions)
    }

    private fun refreshServingUi(item: FoodItem, portions: Map<TextView, Double>) {
        portions.forEach { (view, value) ->
            val isSelected = kotlin.math.abs(value - servingMultiplier) < 0.001
            view.background = createPortionBackground(isSelected)
            view.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
        }

        binding.tvCaloriesValue.text = "${item.calories.scaledDisplayValue()}\nselected amount"
        binding.tvProteinValue.text = item.proteins.scaledDisplayValue()
        binding.tvCarbsValue.text = item.carbs.scaledDisplayValue()
        binding.tvFatValue.text = item.fat.scaledDisplayValue()

        binding.progressProtein.progress = (item.proteins.toScaledDoubleValue() * 2).toInt().coerceIn(0, 100)
        binding.progressCarbs.progress = (item.carbs.toScaledDoubleValue() * 2).toInt().coerceIn(0, 100)
        binding.progressFat.progress = (item.fat.toScaledDoubleValue() * 2).toInt().coerceIn(0, 100)
        setupNutrientsList(item)
    }

    private fun createPortionBackground(isSelected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(if (isSelected) "#EEEBDD" else "#FFFFFF"))
            setStroke(dpToPx(1), Color.parseColor("#DADADA"))
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

    private fun String?.toIntValue(): Int {
        if (this.isNullOrBlank()) return 0
        return this.replace(',', '.')
            .filter { it.isDigit() || it == '.' }
            .toFloatOrNull()
            ?.toInt()
            ?: 0
    }

    private fun String?.toScaledIntValue(): Int {
        return (this.toDoubleValue() * servingMultiplier).roundToInt()
    }

    private fun String?.toDoubleValue(): Double {
        if (this.isNullOrBlank()) return 0.0
        return this.replace(',', '.')
            .filter { it.isDigit() || it == '.' }
            .toDoubleOrNull()
            ?: 0.0
    }

    private fun String?.toScaledDoubleValue(): Double {
        return this.toDoubleValue() * servingMultiplier
    }

    private fun String?.scaledDisplayValue(): String {
        val original = this.orEmpty()
        val unit = original.replace(Regex("[0-9.,\\s]"), "").ifBlank { "" }
        val value = original.toScaledDoubleValue()
        return if (unit.isBlank()) {
            formatAmount(value)
        } else {
            "${formatAmount(value)} $unit"
        }
    }

    private fun formatAmount(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupAiUI(response: AnalysisResponse) {
        binding.portionSelectorCard.visibility = View.GONE
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
        binding.tvVerdictStatus.text = if (score < 40) "Dangerous" else "Safe"
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

        binding.btnAddFood.setOnClickListener {
            val macros = response.macros
            val aiItem = FoodItem(
                title = response.product_type?.takeUnless { it == "unknown" } ?: "AI Analysis",
                subtitle = "AI Scan",
                imageUrl = null,
                calories = "${(macros?.calories ?: 0.0).roundToInt()} kcal",
                proteins = "${macros?.proteins ?: 0.0}g",
                carbs = "${macros?.carbs ?: 0.0}g",
                fat = "${macros?.fats ?: 0.0}g",
                grade = binding.tvGradeBadge.text?.toString() ?: "B",
                ingredients = response.verdict ?: ""
            )
            updateUserCalories(aiItem)
        }

        setupNutrientsFromAi(response.risks ?: emptyList())
    }

    private fun setupNutrientsFromAi(risks: List<String>) {
        binding.nutrientsContainer.removeAllViews()
        if (risks.isEmpty()) {
            addNutrientRow("Health Risks", "None detected")
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

    companion object {
        private val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}
