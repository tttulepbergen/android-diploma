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

        // 1. Проверяем, пришли ли данные от ИИ
        val aiResponse = arguments?.getSerializable("ai_analysis") as? AnalysisResponse

        // 2. Проверяем, пришел ли обычный продукт
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

        // Парсим значения для прогресс-баров
        val p = item.proteins.replace(',', '.').filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
        val c = item.carbs.replace(',', '.').filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
        val f = item.fat.replace(',', '.').filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f

        // Устанавливаем прогресс (умножаем на 2, чтобы 50г был полным кругом, или настрой под себя)
        binding.progressProtein.progress = (p * 2).toInt()
        binding.progressCarbs.progress = (c * 2).toInt()
        binding.progressFat.progress = (f * 2).toInt()

        // 1. Калории (используем значение, которое подготовил toFoodItem)
        binding.tvCaloriesValue.text = "${item.calories}\nper serving"

        binding.tvGradeBadge.text = item.grade ?: "B"
        setupGradeColor(item.grade)

        // 2. ЦЕНТРАЛЬНЫЕ КРУГИ (Берем напрямую из FoodItem)
        // Теперь здесь будет не "0g", а то же самое, что в списке внизу
        binding.tvProteinValue.text = item.proteins
        binding.tvCarbsValue.text = item.carbs
        binding.tvFatValue.text = item.fat

        // 3. НИЖНИЙ СПИСОК
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

        // Расширяем список нутриентов для информативности
        // Все эти поля мы добавили в FoodItem и заполнили в toFoodItem
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
            // Проверяем на null или пустую строку, чтобы не плодить пустые строки
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
        binding.tvProductName.text = "AI Analysis Result"
        binding.tvCategoryLabel.text = "SCANNER"

        // Превращаем числовой score (0-100) в букву (A-E)
        val aiGrade = when {
            response.health_score >= 80 -> "A"
            response.health_score >= 60 -> "B"
            response.health_score >= 40 -> "C"
            response.health_score >= 20 -> "D"
            else -> "E"
        }

        binding.tvGradeBadge.text = aiGrade
        setupGradeColor(aiGrade) // Вызываем твой метод для покраски фона

        // Устанавливаем текст вердикта
        binding.tvAiVerdictDescription.text = response.verdict

        // Заполняем список рисками
        setupNutrientsFromAi(response.risks)

        // Убираем кнопку "Add this food", так как у нас нет полных данных для трекера
        binding.btnAddFood.visibility = View.GONE
    }

    private fun setupNutrientsFromAi(risks: List<String>) {
        binding.nutrientsContainer.removeAllViews()

        if (risks.isEmpty()) {
            addNutrientRow("Health Risks", "None detected ✨")
        } else {
            risks.forEach { risk ->
                // Используем ту же функцию отрисовки строк, что и для обычных продуктов
                addNutrientRow("Risk", risk)
            }
        }

        // Скрываем прогресс-бары КБЖУ, так как ИИ при простом сканировании рисков
        // может не выдать точные граммовки (если твой сервер их не шлет)
        binding.progressProtein.visibility = View.INVISIBLE
        binding.progressCarbs.visibility = View.INVISIBLE
        binding.progressFat.visibility = View.INVISIBLE
        binding.tvCaloriesValue.text = "AI Scan\nComplete"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }}