package com.example.scanfit.mainNavigation.browse.products

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
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
import androidx.core.view.isVisible
import coil.load
import com.example.scanfit.R
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.data.FavoriteProduct
import com.example.scanfit.data.FoodItem
import com.example.scanfit.data.RecentProduct
import com.example.scanfit.databinding.FragmentProductDetailBinding
import com.example.scanfit.mainNavigation.TrackerViewModel
import com.example.scanfit.model.CreateHistoryRequest
import com.example.scanfit.model.CreateLikeRequest
import com.example.scanfit.model.CreateProductScanRequest
import com.example.scanfit.model.CreateUserDailyEatRequest
import com.example.scanfit.model.UpdateUserCaloriesRequest
import com.example.scanfit.model.UserCaloriesData
import com.example.scanfit.network.AnalysisDietConflict
import com.example.scanfit.network.AnalysisDailyImpact
import com.example.scanfit.network.AnalysisDailyImpactItem
import com.example.scanfit.network.AnalysisResponse
import com.example.scanfit.network.AnalysisRisk
import com.example.scanfit.network.KaspiProductItem
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.launch
import com.example.scanfit.mainNavigation.scan.FoodAnalyzer
import com.google.gson.GsonBuilder
import org.json.JSONObject
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    // ─── Design tokens ────────────────────────────────────────────────────────
    companion object {
        private val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Colors
        const val COLOR_BLUE_ACCENT   = "#5CA2F8"
        const val COLOR_GREEN_ACCENT  = "#6FCF97"
        const val COLOR_TEXT_PRIMARY  = "#333333"
        const val COLOR_TEXT_SECONDARY = "#999999"
        const val COLOR_BG_PAGE       = "#F5F5F5"
        const val COLOR_CARD_BG       = "#FFFFFF"
        const val COLOR_DIVIDER       = "#EEEEEE"
        const val COLOR_ICON_NEUTRAL  = "#AAAAAA"

        // Radius (dp)
        const val RADIUS_CARD = 16
        const val RADIUS_BTN  = 16
    }
    // ──────────────────────────────────────────────────────────────────────────

    private val trackerViewModel: TrackerViewModel by activityViewModels()
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val gson = GsonBuilder().serializeNulls().create()
    private var servingMultiplier = 1.0
    private var hasProductDetails = false
    private var currentProductScanId: Int? = null
    private var currentAiResponse: AnalysisResponse? = null
    private var currentFoodItem: FoodItem? = null
    private var kaspiSectionView: LinearLayout? = null
    private var kaspiContentView: LinearLayout? = null
    private val isVipUser: Boolean
        get() = sessionManager.isVip()

    // Maps a nutrient name to a drawable resource (outline/line icons)
    private fun nutrientIconRes(name: String): Int = when {
        name.contains("fat", ignoreCase = true)         -> R.drawable.ic_fats
        name.contains("protein", ignoreCase = true)     -> R.drawable.ic_proteins
        name.contains("carb", ignoreCase = true)        -> R.drawable.ic_carbs
        name.contains("sugar", ignoreCase = true)       -> R.drawable.ic_sugar
        name.contains("fiber", ignoreCase = true)       -> R.drawable.ic_fiber
        name.contains("sodium", ignoreCase = true)      -> R.drawable.ic_sodium
        name.contains("cholesterol", ignoreCase = true) -> R.drawable.ic_cholesterol
        name.contains("vitamin", ignoreCase = true)     -> R.drawable.ic_vitamin
        else                                            -> R.drawable.ic_nutrition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        val aiResponse = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("ai_analysis", AnalysisResponse::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("ai_analysis") as? AnalysisResponse
        }

        val foodItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("foodItem", FoodItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("foodItem") as? FoodItem
        }

        currentFoodItem = foodItem

        if (aiResponse != null) {
            setupAiUI(aiResponse)
            saveToRecent(aiResponse.toFoodItem(), source = "scan")
        } else if (foodItem != null) {
            setupUI(foodItem)
            saveToRecent(foodItem, source = "openfoodfacts")
            loadSavedProductScan(foodItem.title)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnBuyPro.setOnClickListener {
            findNavController().navigate(R.id.action_productDetailFragment_to_proSubscriptionFragment)
        }
        binding.btnCompare.setOnClickListener {
            findNavController().navigate(R.id.action_productDetailFragment_to_compareProductsFragment)
        }
        configureProBanner()
    }

    // ─── Pro Banner ───────────────────────────────────────────────────────────
    private fun configureProBanner() {
        val isBasicUser = sessionManager.fetchUserRole() == "basic"
        binding.cardProBanner.isVisible = isBasicUser
        if (!isBasicUser) return

        binding.btnBuyPro.text = "Buy Pro"
        // Flat green button — no neumorphic shadow, no gradient
        binding.btnBuyPro.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(COLOR_GREEN_ACCENT))
    }

    // ─── AI Analysis trigger ──────────────────────────────────────────────────
    private fun startAiAnalysis(item: FoodItem) {
        val queryText = if (item.ingredients.isNullOrBlank()) {
            "Product: ${item.title}. Ingredients unknown, please analyze based on general knowledge of this product."
        } else {
            item.ingredients
        }

        binding.aiProgressBar.visibility = View.VISIBLE
        binding.tvAiVerdictDescription.text = "AI is analyzing…"
        binding.aiSectionsContainer.visibility = View.GONE
        binding.aiSectionsContainer.removeAllViews()
        binding.btnAnalyzeAi.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrBlank()) {
                    binding.aiProgressBar.visibility = View.GONE
                    binding.btnAnalyzeAi.isEnabled = true
                    binding.tvAiVerdictDescription.text = "User token not found"
                    Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (!hasAvailableProductScanLimit(token)) {
                    binding.aiProgressBar.visibility = View.GONE
                    binding.btnAnalyzeAi.isEnabled = true
                    binding.aiSectionsContainer.visibility = View.GONE
                    return@launch
                }

                val userProfileJson = buildUserProfileJson()
                val response = FoodAnalyzer.analyzeTextIngredientsFull(
                    ingredients = queryText,
                    healthInfo = buildHealthProfileText(userProfileJson),
                    productJson = buildProductJson(item),
                    userProfileJson = userProfileJson
                )

                binding.aiProgressBar.visibility = View.GONE
                if (!hasProductDetails) handleProductType(response.product_type)
                setupAiUI(response)
                decreaseProductScanLimit(token)
                saveProductScan(item.title, response)

            } catch (e: Exception) {
                Log.e("AI_DEBUG", "AI Analysis FAILED: ${e.message}")
                binding.aiProgressBar.visibility = View.GONE
                binding.btnAnalyzeAi.isEnabled = true
                binding.aiSectionsContainer.visibility = View.GONE
                binding.tvAiVerdictDescription.text = "AI Analysis unavailable. Showing standard info."
            }
        }
    }

    private suspend fun hasAvailableProductScanLimit(token: String): Boolean {
        val limitResponse = NetworkClient.userApiService.getProductScanLimit(token)
        if (limitResponse.success == false && limitResponse.data == null) {
            val message = limitResponse.message ?: "Failed to load scan limit"
            binding.tvAiVerdictDescription.text = message
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return false
        }
        val limitData = limitResponse.data
        val isUnlimited = limitData?.isUnlimited == true
        val isExceeded = limitData?.isExceeded == true
        val remainingLimit = limitData?.remaining ?: 0
        if (!isUnlimited && (isExceeded || remainingLimit <= 0)) {
            val message = limitResponse.message ?: "You have no scans left"
            binding.tvAiVerdictDescription.text = message
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private suspend fun decreaseProductScanLimit(token: String) {
        val decreaseResponse = NetworkClient.userApiService.decreaseProductScanLimit(token)
        if (decreaseResponse.success == false) {
            Log.w("PRODUCT_SCAN_LIMIT", "Failed to decrease scan limit: ${decreaseResponse.message}")
        }
    }

    private suspend fun saveProductScan(productName: String, response: AnalysisResponse) {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) return
        runCatching {
            val request = CreateProductScanRequest(
                productName = productName,
                scanInformation = gson.toJsonTree(response)
            )
            val scanId = currentProductScanId
            if (scanId != null) {
                NetworkClient.userApiService.updateProductScan(token, scanId, request)
            } else {
                NetworkClient.userApiService.createProductScan(token, request).also {
                    refreshCurrentProductScanId(token, productName)
                }
            }
        }.onFailure { e -> Log.e("PRODUCT_SCAN", "Failed to save product scan", e) }
    }

    private suspend fun refreshCurrentProductScanId(token: String, productName: String) {
        runCatching {
            val response = NetworkClient.userApiService.getProductScanByProductName(token, productName)
            currentProductScanId = extractSavedScanRecord(response.data)?.id
        }.onFailure { e -> Log.e("PRODUCT_SCAN", "Failed to refresh scan id", e) }
    }

    private fun loadSavedProductScan(productName: String) {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) return

        binding.aiProgressBar.visibility = View.VISIBLE
        binding.tvAiVerdictDescription.text = "Checking saved AI analysis…"
        binding.btnAnalyzeAi.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                NetworkClient.userApiService.getProductScanByProductName(token, productName)
            }.onSuccess { response ->
                binding.aiProgressBar.visibility = View.GONE
                binding.btnAnalyzeAi.isEnabled = true
                val scanRecord = extractSavedScanRecord(response.data)
                currentProductScanId = scanRecord?.id
                val scanInfo = scanRecord?.scanInformation
                if (scanInfo != null && !scanInfo.isJsonNull) {
                    val savedAnalysis = parseSavedAnalysis(scanInfo)
                    if (savedAnalysis != null) setupAiUI(savedAnalysis) else resetAiVerdictState()
                } else {
                    resetAiVerdictState()
                }
            }.onFailure {
                binding.aiProgressBar.visibility = View.GONE
                binding.btnAnalyzeAi.isEnabled = true
                resetAiVerdictState()
            }
        }
    }

    private fun extractSavedScanRecord(data: com.google.gson.JsonElement?): SavedScanRecord? {
        if (data == null || data.isJsonNull) return null
        return runCatching {
            val obj = when {
                data.isJsonArray  -> data.asJsonArray.firstOrNull()?.takeIf { it.isJsonObject }?.asJsonObject
                data.isJsonObject -> data.asJsonObject
                else              -> null
            }
            obj?.let {
                SavedScanRecord(
                    id = it.get("id")?.takeIf { id -> !id.isJsonNull }?.asInt,
                    scanInformation = it.get("scan_information")
                )
            }
        }.getOrNull()
    }

    private data class SavedScanRecord(val id: Int?, val scanInformation: com.google.gson.JsonElement?)

    private fun parseSavedAnalysis(scanInfo: com.google.gson.JsonElement): AnalysisResponse? {
        return runCatching {
            if (scanInfo.isJsonPrimitive && scanInfo.asJsonPrimitive.isString)
                gson.fromJson(scanInfo.asString, AnalysisResponse::class.java)
            else
                gson.fromJson(scanInfo, AnalysisResponse::class.java)
        }.getOrNull()
    }

    private fun handleProductType(type: String?) {
        if (type == "pharmacy") {
            binding.macrosContainer.visibility = View.GONE
            binding.tvVerdictStatus.text = "Pharmacy Info"
            binding.tvVerdictStatus.setTextColor(Color.parseColor(COLOR_BLUE_ACCENT))
        } else {
            binding.macrosContainer.visibility = View.VISIBLE
        }
    }

    // ─── Main UI setup (FoodItem) ─────────────────────────────────────────────
    private fun setupUI(item: FoodItem) {
        hasProductDetails = true
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
        resetAiVerdictState()

        binding.btnAddFood.setOnClickListener { updateUserCalories(item) }
        binding.btnAnalyzeAi.setOnClickListener { startAiAnalysis(item) }
    }

    // ─── Calories update ──────────────────────────────────────────────────────
    private fun updateUserCalories(item: FoodItem) {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDate = trackerViewModel.selectedDate.value ?: Calendar.getInstance()
        val firstAvailableDate = trackerViewModel.firstAvailableDate.value
            ?: parseApiDate(sessionManager.fetchUserFirstDay())
        if (firstAvailableDate != null && selectedDate.normalizedCopy().before(firstAvailableDate.normalizedCopy())) {
            Toast.makeText(
                requireContext(),
                "No information. Tracking starts from ${API_DATE_FORMAT.format(firstAvailableDate.time)}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val day          = API_DATE_FORMAT.format(selectedDate.time)
        val calories     = item.calories.toScaledIntValue()
        val carbs        = item.carbs.toScaledIntValue()
        val fat          = item.fat.toScaledIntValue()
        val proteins     = item.proteins.toScaledIntValue()
        val fiber        = item.fiber.toScaledIntValue()
        val sodium       = item.sodium.toScaledIntValue()
        val sugar        = item.sugars.toScaledIntValue()
        val cholesterol  = item.cholesterol.toScaledIntValue()
        val vitaminA     = item.vitaminA.toScaledDoubleValue()
        val vitaminB12   = item.vitaminB12.toScaledDoubleValue()
        val vitaminB6    = item.vitaminB6.toScaledDoubleValue()
        val vitaminB9    = item.vitaminB9.toScaledDoubleValue()
        val vitaminC     = item.vitaminC.toScaledDoubleValue()
        val vitaminD     = item.vitaminD.toScaledDoubleValue()
        val vitaminE     = item.vitaminE.toScaledDoubleValue()

        val dailyEatRequest = CreateUserDailyEatRequest(
            calorie = calories, carbohydrate = carbs, cholesterol = cholesterol,
            fats = fat, fiber = fiber, portion = servingMultiplier,
            productName = item.title, protein = proteins, sodium = sodium, sugar = sugar,
            vitaminA = vitaminA, vitaminB12 = vitaminB12, vitaminB6 = vitaminB6,
            vitaminB9 = vitaminB9, vitaminC = vitaminC, vitaminD = vitaminD, vitaminE = vitaminE
        )
        val caloriesRequest = UpdateUserCaloriesRequest(
            calories = calories, carbs = carbs, fat = fat, proteins = proteins,
            fiber = fiber, sodium = sodium, sugar = sugar, cholesterol = cholesterol,
            vitaminA = vitaminA, vitaminB12 = vitaminB12, vitaminB6 = vitaminB6,
            vitaminB9 = vitaminB9, vitaminC = vitaminC, vitaminD = vitaminD, vitaminE = vitaminE
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                sessionManager.refreshUserRole()
                val createResponse = NetworkClient.userApiService.createUserDailyEat(token, dailyEatRequest)
                if (createResponse.success == false) {
                    Toast.makeText(requireContext(), createResponse.message ?: "Failed to add food", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val caloriesResponse = NetworkClient.userApiService.updateUserCalories(token, day, caloriesRequest)
                if (caloriesResponse.success && caloriesResponse.data != null) {
                    applyUpdatedCalories(caloriesResponse.data)
                    Toast.makeText(requireContext(), "${item.title} added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), caloriesResponse.message ?: "Failed to update calories", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Failed to update calories", Toast.LENGTH_SHORT).show()
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

    // ─── Grade badge ──────────────────────────────────────────────────────────
    private fun setupGradeColor(grade: String?) {
        val color = when (grade?.uppercase()) {
            "A"  -> "#2E7D32"
            "B"  -> "#8BC34A"
            "C"  -> "#FBC02D"
            "D"  -> "#F57C00"
            "E"  -> "#D32F2F"
            else -> "#BDBDBD"
        }
        binding.tvGradeBadge.background?.setTint(Color.parseColor(color))
    }

    // ─── Nutrients list ───────────────────────────────────────────────────────
    private fun setupNutrientsList(item: FoodItem) {
        binding.nutrientsContainer.removeAllViews()
        val nutrientMap = linkedMapOf(
            "Total Fat"              to item.fat.scaledDisplayValue(),
            "Protein"                to item.proteins.scaledDisplayValue(),
            "Total Carbohydrate"     to item.carbs.scaledDisplayValue(),
            "Sugar"                  to item.sugars.scaledDisplayValue(),
            "Fiber"                  to item.fiber.scaledDisplayValue(),
            "Sodium"                 to item.sodium.scaledDisplayValue(),
            "Cholesterol"            to item.cholesterol.scaledDisplayValue(),
            "Vitamin D"              to item.vitaminD.scaledDisplayValue(),
            "Vitamin B12"            to item.vitaminB12.scaledDisplayValue(),
            "Vitamin C"              to item.vitaminC.scaledDisplayValue(),
            "Vitamin A"              to item.vitaminA.scaledDisplayValue(),
            "Vitamin B6"             to item.vitaminB6.scaledDisplayValue(),
            "Vitamin B9 (Folic acid)" to item.vitaminB9.scaledDisplayValue(),
            "Vitamin E"              to item.vitaminE.scaledDisplayValue()
        )
        for ((name, value) in nutrientMap) {
            addNutrientRow(name, if (value.isNullOrBlank()) "0g" else value)
        }
    }

    /**
     * Unified flat nutrient row:
     *  [name]  [value]  [outline icon — same size, neutral color]
     */
    private fun addNutrientRow(name: String, value: String) {
        val ctx = requireContext()

        val rowLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val vPad = dpToPx(11)
            setPadding(0, vPad, 0, vPad)
            gravity = Gravity.CENTER_VERTICAL
        }

        val nameView = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = name
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }

        val valueView = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = value
            setPadding(0, 0, dpToPx(10), 0)
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
        }

        // Outline icon — uniform size, neutral color
        val iconSize = dpToPx(20)
        val iconView = ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            setImageResource(nutrientIconRes(name))
            imageTintList = ColorStateList.valueOf(Color.parseColor(COLOR_ICON_NEUTRAL))
        }

        rowLayout.addView(nameView)
        rowLayout.addView(valueView)
        rowLayout.addView(iconView)

        val divider = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
            )
            setBackgroundColor(Color.parseColor(COLOR_DIVIDER))
        }

        binding.nutrientsContainer.addView(rowLayout)
        binding.nutrientsContainer.addView(divider)
    }

    // ─── Serving selector ─────────────────────────────────────────────────────
    private fun setupServingSelector(item: FoodItem) {
        currentAiResponse = null
        val portions = portionMap()

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

    private fun setupAiServingSelector(response: AnalysisResponse) {
        currentAiResponse = response
        servingMultiplier = 1.0
        binding.portionSelectorCard.visibility = View.VISIBLE
        binding.tvPortionTitle.text = "Amount"
        binding.tvServingUnit.text = "portion"

        val portions = portionMap()

        portions.forEach { (view, value) ->
            view.setOnClickListener {
                servingMultiplier = value
                binding.etPortionAmount.setText(formatAmount(value))
                binding.etPortionAmount.setSelection(binding.etPortionAmount.text?.length ?: 0)
                refreshAiServingUi(response, portions)
            }
        }

        binding.etPortionAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (currentAiResponse !== response) return
                servingMultiplier = s.toString().replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                refreshAiServingUi(response, portions)
            }
        })

        binding.etPortionAmount.setText(formatAmount(servingMultiplier))
        binding.etPortionAmount.setSelection(binding.etPortionAmount.text?.length ?: 0)
        refreshAiServingUi(response, portions)
    }

    private fun portionMap(): LinkedHashMap<TextView, Double> = linkedMapOf(
        binding.btnPortionQuarter       to 0.25,
        binding.btnPortionHalf          to 0.5,
        binding.btnPortionThreeQuarters to 0.75,
        binding.btnPortionOne           to 1.0,
        binding.btnPortionOneHalf       to 1.5,
        binding.btnPortionTwo           to 2.0,
        binding.btnPortionTwoHalf       to 2.5,
        binding.btnPortionThree         to 3.0
    )

    private fun refreshServingUi(item: FoodItem, portions: Map<TextView, Double>) {
        portions.forEach { (view, value) ->
            val selected = kotlin.math.abs(value - servingMultiplier) < 0.001
            view.background = createPortionBackground(selected)
            view.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
        binding.tvCaloriesValue.text = "${item.calories.scaledDisplayValue()}\nselected amount"
        binding.tvProteinValue.text = item.proteins.scaledDisplayValue()
        binding.tvCarbsValue.text   = item.carbs.scaledDisplayValue()
        binding.tvFatValue.text     = item.fat.scaledDisplayValue()
        binding.progressProtein.progress = (item.proteins.toScaledDoubleValue() * 2).toInt().coerceIn(0, 100)
        binding.progressCarbs.progress  = (item.carbs.toScaledDoubleValue() * 2).toInt().coerceIn(0, 100)
        binding.progressFat.progress    = (item.fat.toScaledDoubleValue() * 2).toInt().coerceIn(0, 100)
        setupNutrientsList(item)
    }

    private fun refreshAiServingUi(response: AnalysisResponse, portions: Map<TextView, Double>) {
        portions.forEach { (view, value) ->
            val selected = kotlin.math.abs(value - servingMultiplier) < 0.001
            view.background = createPortionBackground(selected)
            view.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
        val macros   = response.macros
        val calories = (macros?.calories ?: 0.0) * servingMultiplier
        val protein  = (macros?.proteins ?: 0.0) * servingMultiplier
        val carbs    = (macros?.carbs ?: 0.0) * servingMultiplier
        val fat      = (macros?.fats ?: macros?.fat ?: 0.0) * servingMultiplier

        binding.tvCaloriesValue.text = "${formatAmount(calories)} kcal\nselected amount"
        binding.tvProteinValue.text  = "${formatAmount(protein)}g"
        binding.tvCarbsValue.text    = "${formatAmount(carbs)}g"
        binding.tvFatValue.text      = "${formatAmount(fat)}g"
        binding.progressProtein.progress = (protein * 2).toInt().coerceIn(0, 100)
        binding.progressCarbs.progress  = (carbs * 2).toInt().coerceIn(0, 100)
        binding.progressFat.progress    = (fat * 2).toInt().coerceIn(0, 100)
    }

    /**
     * Flat portion circle:
     *  selected  → filled with soft blue, white text
     *  default   → white fill, light grey stroke
     */
    private fun createPortionBackground(isSelected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (isSelected) {
                setColor(Color.parseColor(COLOR_BLUE_ACCENT))
            } else {
                setColor(Color.WHITE)
                setStroke(dpToPx(1), Color.parseColor("#DDDDDD"))
            }
        }
    }

    // ─── Reset AI state ───────────────────────────────────────────────────────
    private fun resetAiVerdictState() {
        currentAiResponse = null
        binding.aiProgressBar.visibility = View.GONE
        binding.btnAnalyzeAi.visibility  = View.VISIBLE
        binding.btnAnalyzeAi.isEnabled   = true
        binding.btnAnalyzeAi.text        = "Analyze with AI"
        binding.tvVerdictStatus.text     = "Not analyzed yet"
        binding.tvVerdictStatus.setTextColor(Color.parseColor("#6B7280"))
        binding.tvAiVerdictDescription.text = "Tap Analyze with AI to check this product against your profile."
        binding.aiSectionsContainer.visibility = View.GONE
        binding.aiSectionsContainer.removeAllViews()
        binding.portionSelectorCard.visibility = if (hasProductDetails) View.VISIBLE else View.GONE
    }

    // ─── AI UI setup ──────────────────────────────────────────────────────────
    private fun setupAiUI(response: AnalysisResponse) {
        currentAiResponse = response
        binding.aiProgressBar.visibility = View.GONE
        binding.btnAnalyzeAi.visibility  = if (hasProductDetails) View.VISIBLE else View.GONE
        binding.btnAnalyzeAi.isEnabled   = true
        binding.btnAnalyzeAi.text        = "Analyze again"

        if (!hasProductDetails) {
            val aiItem = response.toFoodItem()
            viewLifecycleOwner.lifecycleScope.launch {
                val existing = database.productDao().getFavoriteById(aiItem.title)
                aiItem.isFavorite = existing != null
                updateFavoriteIcon(aiItem.isFavorite)
            }
            binding.ivFavorite.setOnClickListener {
                aiItem.isFavorite = !aiItem.isFavorite
                updateFavoriteIcon(aiItem.isFavorite)
                handleFavoriteAction(aiItem, source = "scan")
            }
        }

        if (!hasProductDetails) {
            binding.tvProductName.text     = response.displayName()
            binding.tvCategoryLabel.text   = response.product_type?.uppercase(Locale.US) ?: "SCANNER"
            binding.ivProductImage.load(
                response.scanImage?.url ?: response.scanImageUrl ?: response.imagePath ?: response.productPhoto?.imageUrl
            ) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
            setupAiServingSelector(response)
        }

        val score = response.health_score ?: 0
        val (color, grade) = when {
            score >= 80 -> "#4CAF50" to "A"
            score >= 60 -> "#8BC34A" to "B"
            score >= 40 -> "#FBC02D" to "C"
            else        -> "#F44336" to "E"
        }

        if (!hasProductDetails) {
            binding.tvGradeBadge.text = grade
            binding.tvGradeBadge.background?.setTint(Color.parseColor(color))
        }
        binding.tvVerdictStatus.setTextColor(Color.parseColor(color))

        val riskLabel = response.risk_level?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }
        binding.tvVerdictStatus.text        = riskLabel ?: if (score < 40) "Dangerous" else "Safe"
        binding.tvAiVerdictDescription.text = response.verdict ?: "No description"
        renderAiSections(response)

        val kaspiQuery = response.alternatives.orEmpty()
            .mapNotNull { it.name?.trim() }.filter { it.isNotBlank() }.take(5)
            .joinToString(",")
        if (kaspiQuery.isNotBlank()) {
            loadKaspiProducts(kaspiQuery)
        }

        if (!hasProductDetails) response.macros?.let { m ->
            val cal  = m.calories ?: 0.0
            val prot = m.proteins ?: 0.0
            val carb = m.carbs ?: 0.0
            val fat  = m.fats ?: m.fat ?: 0.0
            binding.tvCaloriesValue.text = "$cal kcal\nper portion"
            binding.tvProteinValue.text  = "${prot}g"
            binding.tvCarbsValue.text    = "${carb}g"
            binding.tvFatValue.text      = "${fat}g"
            binding.progressProtein.progress = (prot * 2).toInt()
            binding.progressCarbs.progress  = (carb * 2).toInt()
            binding.progressFat.progress    = (fat * 2).toInt()
        }

        if (!hasProductDetails) {
            binding.btnAddFood.setOnClickListener {
                val macros = response.macros
                val aiItem = FoodItem(
                    title       = response.displayName(),
                    subtitle    = "AI Scan",
                    imageUrl    = null,
                    calories    = "${(macros?.calories ?: 0.0).roundToInt()} kcal",
                    proteins    = "${macros?.proteins ?: 0.0}g",
                    carbs       = "${macros?.carbs ?: 0.0}g",
                    fat         = "${(macros?.fats ?: macros?.fat ?: 0.0)}g",
                    grade       = binding.tvGradeBadge.text?.toString() ?: "B",
                    sugars      = "${macros?.sugar ?: 0.0}g",
                    fiber       = "${macros?.fiber ?: 0.0}g",
                    sodium      = "${macros?.sodium ?: 0.0}mg",
                    cholesterol = "${macros?.cholesterol ?: 0.0}mg",
                    ingredients = response.verdict ?: ""
                )
                updateUserCalories(aiItem)
            }
        }

        if (!hasProductDetails) setupNutrientsFromAi(response.risks ?: emptyList())
    }

    // ─── AI sections rendering ────────────────────────────────────────────────
    private fun renderAiSections(response: AnalysisResponse) {
        kaspiSectionView = null
        kaspiContentView = null
        binding.aiSectionsContainer.removeAllViews()

        val risks            = response.risks.orEmpty()
        val conflicts        = response.diet_conflicts.orEmpty()
        val sources          = response.sources.orEmpty()
        val alternatives     = response.alternatives.orEmpty()
        val ingredients      = response.identifiedIngredients.orEmpty()
        val compounds        = response.compounds.toCompoundItems()
        val dailyImpactItems = extractDailyImpactItems(response.dailyImpact, isVipUser)

        response.estimatedServing?.let { serving ->
            val value = buildString {
                serving.amount?.let { append(formatAmount(it)) }
                serving.unit?.takeIf { it.isNotBlank() }?.let {
                    if (isNotEmpty()) append(" "); append(it)
                }
                if (isBlank()) append("Not provided")
            }
            addAiSection("Serving", "Estimated portion size", "#F7F7FF") {
                addAiMetricRow("Estimated serving", value)
                serving.description?.takeIf { it.isNotBlank() }?.let { addAiTextRow("Description", it) }
                response.estimationConfidence?.takeIf { it.isNotBlank() }?.let { c ->
                    addAiMetricRow("Confidence", c.replace('_', ' ').replaceFirstChar { ch ->
                        if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
                    })
                }
            }
        }

        if (ingredients.isNotEmpty()) {
            addAiSection("Ingredients", "Detected in the dish", "#EEF6FF") {
                ingredients.forEach { ingredient ->
                    val body = buildString {
                        ingredient.estimatedAmount?.takeIf { it.isNotBlank() }?.let { append("Estimated amount: $it") }
                        ingredient.confidence?.takeIf { it.isNotBlank() }?.let {
                            if (isNotEmpty()) append("\n")
                            append("Confidence: ${it.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString() }}")
                        }
                        if (isEmpty()) append("Detected by AI from the image.")
                    }
                    addAiTextRow(ingredient.name?.takeIf { it.isNotBlank() } ?: "Ingredient", body)
                }
            }
        }

        if (risks.isNotEmpty()) {
            addAiSection("Risks", "What may be a problem", "#FFF3E8") {
                risks.forEach { risk ->
                    addAiIssueRow(
                        title    = risk.ingredient?.takeIf { it.isNotBlank() } ?: "Issue",
                        body     = risk.reason ?: "Needs attention for this profile.",
                        severity = risk.severity
                    )
                }
            }
        }

        if (compounds.isNotEmpty()) {
            addAiSection("Compounds", "Estimated composition of this food", "#F7FBEF") {
                compounds.forEach { (label, value) -> addAiMetricRow(label, value) }
            }
        }

        if (conflicts.isNotEmpty()) {
            addAiSection("Diet conflicts", "Compared with active diets", "#F0F7FF") {
                conflicts.forEach { conflict ->
                    addAiIssueRow(
                        title    = conflict.diet_code?.takeIf { it.isNotBlank() } ?: "Diet conflict",
                        body     = conflict.reason ?: "This may not match one of the selected diets.",
                        severity = conflict.severity
                    )
                }
            }
        }

        if (alternatives.isNotEmpty()) {
            addAiSection("Better choices", "Safer options for your profile", "#EEF8F1") {
                alternatives.take(4).forEach { alt ->
                    addAiTextRow(
                        title = alt.name?.takeIf { it.isNotBlank() } ?: "Alternative",
                        body  = alt.reason ?: "This option may be a better fit for your profile."
                    )
                }
            }
        }

        val kaspiAlternativeNames = response.alternatives.orEmpty()
            .mapNotNull { it.name?.trim() }.filter { it.isNotBlank() }.take(5)
        if (kaspiAlternativeNames.isNotEmpty()) {
            kaspiContentView = buildKaspiLoadingSection()
        }

        if (dailyImpactItems.isNotEmpty()) {
            addAiSection("Daily impact", "How this fits into today's totals", "#FFF9E8") {
                dailyImpactItems.forEach { (label, item) ->
                    addAiMetricRow(
                        title  = label,
                        value  = item.message ?: formatDailyImpactFallback(item),
                        status = item.status
                    )
                }
            }
        }

        if (sources.isNotEmpty()) {
            addAiSection("Sources", "Evidence used by AI", "#F5F5F5") {
                sources.take(4).forEachIndexed { index, source ->
                    val title = source.title?.takeIf { it.isNotBlank() } ?: "Source ${index + 1}"
                    addAiTextRow(
                        title = title,
                        body  = buildString {
                            source.source_type?.takeIf { it.isNotBlank() }?.let {
                                append(it.replace('_', ' ').replaceFirstChar { ch ->
                                    if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
                                })
                            }
                            source.url?.takeIf { it.isNotBlank() }?.let {
                                if (isNotEmpty()) append("\n"); append(it)
                            }
                            if (isEmpty()) append("No link provided")
                        }
                    )
                }
            }
        }

        binding.aiSectionsContainer.visibility =
            if (binding.aiSectionsContainer.childCount == 0) View.GONE else View.VISIBLE
    }

    private fun addAiSection(
        title: String, subtitle: String, backgroundColor: String,
        content: LinearLayout.() -> Unit
    ) {
        val section = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
            background = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(RADIUS_CARD).toFloat()
                setColor(Color.parseColor(backgroundColor))
                setStroke(dpToPx(1), Color.parseColor("#E5E7EB"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dpToPx(10)) }
        }
        section.addView(TextView(requireContext()).apply {
            text = title
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
        })
        section.addView(TextView(requireContext()).apply {
            text = subtitle
            setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(0, dpToPx(2), 0, dpToPx(8))
        })
        section.content()
        binding.aiSectionsContainer.addView(section)
    }

    private fun LinearLayout.addAiIssueRow(title: String, body: String, severity: String?) {
        val severityText  = severity?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() } ?: "Note"
        val severityColor = when (severity?.lowercase(Locale.US)) {
            "high"   -> "#DC2626"
            "medium" -> "#D97706"
            "low"    -> "#2563EB"
            else     -> "#4B5563"
        }
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }
        val header = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(requireContext()).apply {
            text = title
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface  = Typeface.DEFAULT_BOLD
            maxLines  = 2
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        header.addView(TextView(requireContext()).apply {
            text = severityText
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            background = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.parseColor(severityColor))
            }
        })
        row.addView(header)
        row.addView(TextView(requireContext()).apply {
            text = body
            setTextColor(Color.parseColor("#374151"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setLineSpacing(dpToPx(2).toFloat(), 1f)
            setPadding(0, dpToPx(5), 0, 0)
        })
        addView(row)
        addAiDivider()
    }

    private fun LinearLayout.addAiTextRow(title: String, body: String) {
        addView(TextView(requireContext()).apply {
            text = title
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(8), 0, 0)
        })
        addView(TextView(requireContext()).apply {
            text = body
            setTextColor(Color.parseColor("#4B5563"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setLineSpacing(dpToPx(2).toFloat(), 1f)
            setPadding(0, dpToPx(3), 0, dpToPx(8))
        })
        addAiDivider()
    }

    private fun LinearLayout.addAiMetricRow(title: String, value: String, status: String? = null) {
        val isProLocked = status.equals("pro", ignoreCase = true)
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }
        row.addView(TextView(requireContext()).apply {
            text = title
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface     = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f)
        })
        row.addView(TextView(requireContext()).apply {
            text = if (isProLocked) "" else value
            setTextColor(Color.parseColor("#4B5563"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity      = Gravity.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.1f)
        })
        status?.takeIf { it.isNotBlank() }?.let { rawStatus ->
            val statusText = if (isProLocked) "ScanFit Pro"
            else rawStatus.replace('_', ' ').replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
            }
            row.addView(TextView(requireContext()).apply {
                text = statusText
                setTextColor(if (isProLocked) Color.parseColor(COLOR_BLUE_ACCENT) else Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                typeface = Typeface.DEFAULT_BOLD
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = dpToPx(8) }
                background = GradientDrawable().apply {
                    shape        = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(12).toFloat()
                    setColor(if (isProLocked) Color.parseColor("#EAF2FF") else colorForImpactStatus(rawStatus))
                }
            })
        }
        addView(row)
        addAiDivider()
    }

    private fun LinearLayout.addAiDivider() {
        addView(View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1))
            setBackgroundColor(Color.parseColor("#E5E7EB"))
        })
    }

    // ─── Kaspi products ───────────────────────────────────────────────────────
    private fun buildKaspiLoadingSection(): LinearLayout {
        val ctx = requireContext()
        val contentLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        contentLayout.addView(TextView(ctx).apply {
            text = "Searching Kaspi.kz…"
            setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        })
        val section = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(RADIUS_CARD).toFloat()
                setColor(Color.parseColor("#FFF8E7"))
                setStroke(dpToPx(1), Color.parseColor("#E5E7EB"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dpToPx(10)) }
        }
        section.addView(TextView(ctx).apply {
            text = "Available on Kaspi"
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
        })
        section.addView(TextView(ctx).apply {
            text = "Tap a product to open in Kaspi"
            setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(0, dpToPx(2), 0, dpToPx(8))
        })
        section.addView(contentLayout)
        kaspiSectionView = section
        binding.aiSectionsContainer.addView(section)
        binding.aiSectionsContainer.visibility = View.VISIBLE
        return contentLayout
    }

    private fun loadKaspiProducts(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: return@launch
                val result = NetworkClient.userApiService.searchKaspiProducts(token, query)
                val products = result.products.orEmpty()
                val content = kaspiContentView ?: return@launch
                content.removeAllViews()
                if (products.isNotEmpty()) {
                    products.take(20).forEach { product -> content.addKaspiProductCard(product) }
                } else {
                    kaspiSectionView?.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.w("KASPI", "Kaspi search failed for '$query': ${e.message}")
                kaspiSectionView?.visibility = View.GONE
            }
        }
    }

    private fun showKaspiSection(products: List<KaspiProductItem>) {
        addAiSection("Available on Kaspi", "Tap a product to open in Kaspi", "#FFF8E7") {
            products.take(20).forEach { product ->
                addKaspiProductCard(product)
            }
        }
    }

    private fun LinearLayout.addKaspiProductCard(product: KaspiProductItem) {
        val productUrl = product.kaspiUrl ?: return

        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dpToPx(10), 0, dpToPx(10))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                runCatching {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(productUrl)))
                }
            }
        }

        val imageView = ImageView(requireContext()).apply {
            val size = dpToPx(72)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = dpToPx(12)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#F5F5F5"))
            }
            clipToOutline = true
            load(product.previewImage) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
        }
        card.addView(imageView)

        val textCol = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        textCol.addView(TextView(requireContext()).apply {
            text = product.title ?: "Product"
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        })
        textCol.addView(TextView(requireContext()).apply {
            text = buildString {
                product.priceFormatted?.let { append(it) }
                product.rating?.let {
                    if (isNotEmpty()) append("  ")
                    append("⭐ $it")
                    product.reviewsQuantity?.let { qty -> append(" ($qty)") }
                }
                product.discount?.takeIf { it > 0 }?.let {
                    if (isNotEmpty()) append("  ")
                    append("-$it%")
                }
            }
            setTextColor(Color.parseColor("#4B5563"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(0, dpToPx(3), 0, 0)
        })
        textCol.addView(TextView(requireContext()).apply {
            text = "Открыть в Kaspi →"
            setTextColor(Color.parseColor(COLOR_BLUE_ACCENT))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(4), 0, 0)
        })
        card.addView(textCol)
        addView(card)
        addAiDivider()
    }

    // ─── Daily impact ─────────────────────────────────────────────────────────
    private fun extractDailyImpactItems(
        dailyImpact: AnalysisDailyImpact?,
        includePremium: Boolean
    ): List<Pair<String, AnalysisDailyImpactItem>> {
        if (dailyImpact == null) return emptyList()
        val baseItems = listOfNotNull(
            "Calories" to dailyImpact.calories,
            "Carbs"    to dailyImpact.carbs,
            "Protein"  to dailyImpact.proteins,
            "Fat"      to dailyImpact.fat,
            "Water"    to dailyImpact.water
        )
        val premiumItems = listOfNotNull(
            "Sugar" to dailyImpact.sugar, "Fiber" to dailyImpact.fiber,
            "Sodium" to dailyImpact.sodium, "Cholesterol" to null,
            "Vitamin A" to dailyImpact.vitaminA, "Vitamin B12" to dailyImpact.vitaminB12,
            "Vitamin B6" to dailyImpact.vitaminB6, "Vitamin B9" to dailyImpact.vitaminB9,
            "Vitamin C" to dailyImpact.vitaminC, "Vitamin D" to dailyImpact.vitaminD,
            "Vitamin E" to dailyImpact.vitaminE
        )
        val items = if (includePremium) {
            baseItems + premiumItems.filter { (_, item) -> item != null }
        } else {
            baseItems + premiumItems.map { (label, _) -> label to premiumLockedImpactItem() }
        }
        return items.filter { (_, item) -> item != null }.map { (label, item) -> label to item!! }
    }

    private fun premiumLockedImpactItem() = AnalysisDailyImpactItem(
        status  = "pro",
        message = "ScanFit Pro unlocks sugar, fiber, sodium, water, vitamins, and the rest of your daily comparison."
    )

    private fun formatDailyImpactFallback(item: AnalysisDailyImpactItem): String {
        val after = item.afterThisProduct?.let { formatAmount(it) }
        val unit  = item.unit.orEmpty()
        return when {
            after != null && unit.isNotBlank() -> "$after $unit after this product"
            after != null                      -> "$after after this product"
            !item.status.isNullOrBlank()       -> item.status.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
            }
            else -> "No details"
        }
    }

    private fun colorForImpactStatus(status: String): Int = when (status.lowercase(Locale.US)) {
        "within_goal"      -> Color.parseColor("#2E7D32")
        "goal_exceeded"    -> Color.parseColor("#C62828")
        "goal_unavailable" -> Color.parseColor("#6B7280")
        "not_provided"     -> Color.parseColor("#B7791F")
        "pro"              -> Color.parseColor("#111827")
        else               -> Color.parseColor("#4B5563")
    }

    // ─── AI nutrients from risks ──────────────────────────────────────────────
    private fun setupNutrientsFromAi(risks: List<AnalysisRisk>) {
        binding.nutrientsContainer.removeAllViews()
        if (risks.isEmpty()) {
            addNutrientRow("Health Risks", "None detected")
        } else {
            formatRiskLines(risks).forEach { risk -> addNutrientRow("Risk", risk.removePrefix("- ")) }
        }
        binding.progressProtein.visibility = View.INVISIBLE
        binding.progressCarbs.visibility   = View.INVISIBLE
        binding.progressFat.visibility     = View.INVISIBLE
        binding.tvCaloriesValue.text       = "AI Scan\nComplete"
    }

    private fun formatRiskLines(items: List<AnalysisRisk>?): List<String> =
        items.orEmpty().map { item ->
            val name   = item.ingredient?.takeIf { it.isNotBlank() } ?: "Issue"
            val reason = item.reason?.takeIf { it.isNotBlank() } ?: item.severity
            if (reason.isNullOrBlank()) "- $name" else "- $name: $reason"
        }

    private fun formatDietConflictLines(items: List<AnalysisDietConflict>?): List<String> =
        items.orEmpty().map { item ->
            val name   = item.diet_code?.takeIf { it.isNotBlank() } ?: "Diet conflict"
            val reason = item.reason?.takeIf { it.isNotBlank() } ?: item.severity
            if (reason.isNullOrBlank()) "- $name" else "- $name: $reason"
        }

    // ─── Favourites & recents ─────────────────────────────────────────────────
    private fun saveToRecent(item: FoodItem, source: String = "openfoodfacts") {
        val recentProduct = item.asRecentProduct()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                database.productDao().insertRecent(recentProduct)
            } catch (e: Exception) {
                Log.e("ProductDetail", "Error saving to recent: ${e.message}")
            }
            val token = sessionManager.fetchAuthToken() ?: return@launch
            runCatching {
                NetworkClient.userApiService.createHistory(
                    token,
                    CreateHistoryRequest(productName = item.title, productData = gson.toJson(item), source = source)
                )
            }.onFailure { e -> Log.e("ProductDetail", "Error saving to backend history: ${e.message}") }
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        binding.ivFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favourites_liked else R.drawable.ic_favourites
        )
        binding.ivFavorite.setColorFilter(
            Color.parseColor(if (isFavorite) "#FF4B4B" else "#BDBDBD")
        )
    }

    private fun handleFavoriteAction(item: FoodItem, source: String = "openfoodfacts") {
        viewLifecycleOwner.lifecycleScope.launch {
            if (item.isFavorite) {
                var backendId = 0L
                val token = sessionManager.fetchAuthToken()
                if (!token.isNullOrBlank()) {
                    runCatching {
                        NetworkClient.userApiService.createLike(
                            token,
                            CreateLikeRequest(productName = item.title, productData = gson.toJson(item), source = source)
                        )
                    }.onSuccess { response -> backendId = response.data?.id ?: 0L }
                        .onFailure { e -> Log.e("ProductDetail", "Error saving like: ${e.message}") }
                }
                database.productDao().insertFavorite(item.asFavoriteProduct(backendId))
                Toast.makeText(requireContext(), "Saved to favourites", Toast.LENGTH_SHORT).show()
            } else {
                val token = sessionManager.fetchAuthToken()
                if (!token.isNullOrBlank()) {
                    val existing = database.productDao().getFavoriteById(item.title)
                    val likeId   = existing?.backendId ?: 0L
                    if (likeId > 0) {
                        runCatching { NetworkClient.userApiService.deleteLike(token, likeId) }
                            .onFailure { e -> Log.e("ProductDetail", "Error deleting like: ${e.message}") }
                    }
                }
                database.productDao().deleteFavoriteById(item.title)
                Toast.makeText(requireContext(), "Removed from favourites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── Build JSON helpers ───────────────────────────────────────────────────
    private fun buildProductJson(item: FoodItem): String {
        val nutriments = JSONObject().apply {
            putParsed("energy-kcal", item.calories)
            putParsed("energy-kcal_100g", item.calories)
            putParsed("energy-kcal_serving", item.calories)
            put("energy-kcal_unit", "kcal")
            putParsed("proteins", item.proteins); putParsed("proteins_100g", item.proteins); putParsed("proteins_serving", item.proteins); put("proteins_unit", "g")
            putParsed("carbohydrates", item.carbs); putParsed("carbohydrates_100g", item.carbs); putParsed("carbohydrates_serving", item.carbs); put("carbohydrates_unit", "g")
            putParsed("fat", item.fat); putParsed("fat_100g", item.fat); putParsed("fat_serving", item.fat); put("fat_unit", "g")
            putParsed("sugars", item.sugars); putParsed("sugars_100g", item.sugars); putParsed("sugars_serving", item.sugars); put("sugars_unit", "g")
            putParsed("fiber", item.fiber); putParsed("fiber_100g", item.fiber); putParsed("fiber_serving", item.fiber); put("fiber_unit", "g")
            putParsed("sodium", item.sodium); putParsed("sodium_100g", item.sodium); putParsed("sodium_serving", item.sodium); put("sodium_unit", "mg")
            putParsed("cholesterol", item.cholesterol); putParsed("cholesterol_100g", item.cholesterol); putParsed("cholesterol_serving", item.cholesterol); put("cholesterol_unit", "mg")
        }
        val estimated = JSONObject().apply {
            putParsed("vitamin-a_100g", item.vitaminA); putParsed("vitamin-b12_100g", item.vitaminB12)
            putParsed("vitamin-b6_100g", item.vitaminB6); putParsed("vitamin-b9_100g", item.vitaminB9)
            putParsed("vitamin-c_100g", item.vitaminC); putParsed("vitamin-d_100g", item.vitaminD)
            putParsed("vitamin-e_100g", item.vitaminE)
        }
        return JSONObject().apply {
            putNullable("brands", item.subtitle); putNullable("image_url", item.imageUrl)
            put("product_name", item.title); putNullable("ingredients_text", item.ingredients)
            putNullable("ingredients_text_en", item.ingredients)
            put("nutriments", nutriments); put("nutriments_estimated", estimated)
            putNullable("nutriscore_grade", item.grade)
            put("nutrition_data", "on"); put("nutrition_data_per", "100g")
            put("nutrition_data_prepared_per", "100g")
        }.toString()
    }

    private suspend fun buildUserProfileJson(): String {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) return emptyUserDetailsJson().toString()
        val details = runCatching { NetworkClient.userApiService.getUserDetails(token).data }.getOrNull()
        return details?.let { gson.toJson(it) } ?: emptyUserDetailsJson().toString()
    }

    private fun buildHealthProfileText(userProfileJson: String) = "User health profile JSON:\n$userProfileJson"

    private fun JSONObject.putNullable(name: String, value: Any?): JSONObject { put(name, value ?: JSONObject.NULL); return this }
    private fun JSONObject.putParsed(name: String, value: String?): JSONObject { put(name, value.toDoubleValue()); return this }

    private fun emptyUserDetailsJson() = JSONObject().apply {
        put("active_diet_types", org.json.JSONArray())
        put("active_dietary_preferences", org.json.JSONArray())
        put("active_diseases", org.json.JSONArray())
        put("active_health_conditions", org.json.JSONArray())
        put("measure", JSONObject.NULL)
        put("user", JSONObject().apply {
            put("role", JSONObject().apply { putNullable("code", sessionManager.fetchUserRole() ?: "basic") })
        })
        put("weight_management", JSONObject.NULL)
    }

    // ─── Extension converters ─────────────────────────────────────────────────
    private fun String?.toDoubleValue(): Double {
        if (this.isNullOrBlank()) return 0.0
        return replace(',', '.').filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
    }
    private fun String?.toScaledIntValue()    = (this.toDoubleValue() * servingMultiplier).roundToInt()
    private fun String?.toScaledDoubleValue() = this.toDoubleValue() * servingMultiplier
    private fun String?.scaledDisplayValue(): String {
        val original = this.orEmpty()
        val unit  = original.replace(Regex("[0-9.,\\s]"), "").ifBlank { "" }
        val value = original.toScaledDoubleValue()
        return if (unit.isBlank()) formatAmount(value) else "${formatAmount(value)} $unit"
    }

    private fun formatAmount(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString()
        else String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    // ─── FoodItem mappers ─────────────────────────────────────────────────────
    private fun FoodItem.asFavoriteProduct(backendId: Long = 0) = FavoriteProduct(
        id = title, productName = title, name = subtitle ?: "", imageUrl = imageUrl,
        calories = calories, grade = grade, ingredients = ingredients,
        proteins = proteins, fat = fat, carbs = carbs, description = description,
        cholesterol = cholesterol, sodium = sodium, sugars = sugars, fiber = fiber,
        vitaminD = vitaminD, vitaminB12 = vitaminB12, vitaminC = vitaminC,
        vitaminA = vitaminA, vitaminB6 = vitaminB6, vitaminB9 = vitaminB9, vitaminE = vitaminE,
        backendId = backendId, source = source
    )

    private fun FoodItem.asRecentProduct(timestamp: Long = System.currentTimeMillis()) = RecentProduct(
        id = title, title = title, subtitle = subtitle, imageUrl = imageUrl,
        calories = calories, grade = grade, timestamp = timestamp, ingredients = ingredients,
        proteins = proteins, fat = fat, carbs = carbs, description = description,
        cholesterol = cholesterol, sodium = sodium, sugars = sugars, fiber = fiber,
        vitaminD = vitaminD, vitaminB12 = vitaminB12, vitaminC = vitaminC,
        vitaminA = vitaminA, vitaminB6 = vitaminB6, vitaminB9 = vitaminB9, vitaminE = vitaminE,
        source = source
    )

    private fun AnalysisResponse.toFoodItem(): FoodItem {
        val m        = macros
        val imageUrl = scanImage?.url ?: scanImageUrl ?: imagePath ?: productPhoto?.imageUrl
        val score    = health_score ?: 0
        val grade    = when { score >= 80 -> "A"; score >= 60 -> "B"; score >= 40 -> "C"; else -> "E" }
        return FoodItem(
            title       = displayName(),
            subtitle    = product_type?.uppercase(Locale.US) ?: "AI SCAN",
            imageUrl    = imageUrl,
            calories    = "${(m?.calories ?: 0.0).roundToInt()} kcal",
            grade       = grade,
            proteins    = "${m?.proteins ?: 0.0}g",
            fat         = "${(m?.fats ?: m?.fat ?: 0.0)}g",
            carbs       = "${m?.carbs ?: 0.0}g",
            sugars      = "${m?.sugar ?: 0.0}g",
            fiber       = "${m?.fiber ?: 0.0}g",
            sodium      = "${m?.sodium ?: 0.0}mg",
            cholesterol = "${m?.cholesterol ?: 0.0}mg",
            ingredients = verdict ?: "",
            source      = "scan"
        )
    }

    private fun AnalysisResponse.displayName() =
        dishName ?: product_name ?: product_type?.takeUnless { it == "unknown" } ?: "AI Analysis"

    private fun com.example.scanfit.network.AnalysisCompounds?.toCompoundItems(): List<Pair<String, String>> {
        if (this == null) return emptyList()
        return listOfNotNull(
            water?.let { "Water" to "${formatAmount(it)} ml" },
            saturatedFat?.let { "Saturated fat" to "${formatAmount(it)} g" },
            unsaturatedFat?.let { "Unsaturated fat" to "${formatAmount(it)} g" },
            addedSugar?.let { "Added sugar" to "${formatAmount(it)} g" },
            naturalSugar?.let { "Natural sugar" to "${formatAmount(it)} g" },
            starch?.let { "Starch" to "${formatAmount(it)} g" },
            potassium?.let { "Potassium" to "${formatAmount(it)} mg" },
            calcium?.let { "Calcium" to "${formatAmount(it)} mg" },
            iron?.let { "Iron" to "${formatAmount(it)} mg" },
            magnesium?.let { "Magnesium" to "${formatAmount(it)} mg" },
            caffeine?.let { "Caffeine" to "${formatAmount(it)} mg" }
        )
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ─── Date helpers ─────────────────────────────────────────────────────────
    private fun parseApiDate(rawDate: String?): Calendar? {
        if (rawDate.isNullOrBlank()) return null
        return try {
            Calendar.getInstance().apply {
                time = API_DATE_FORMAT.parse(rawDate) ?: return null
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
        } catch (_: ParseException) { null }
    }

    private fun Calendar.normalizedCopy() = (clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
}
