package com.example.scanfit.mainNavigation.user.profile

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentUserProfileBinding
import com.example.scanfit.model.Disease
import com.example.scanfit.model.DiseaseLevel
import com.example.scanfit.model.DietType
import com.example.scanfit.model.UpdateDietTypeRequest
import com.example.scanfit.model.UpdateDiseaseRequest
import com.example.scanfit.model.UpdateUserMeasureRequest
import com.example.scanfit.model.UserAccountData
import com.example.scanfit.model.UserMeasureData
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private enum class DietarySectionType {
        DIET_TYPE,
        DIETARY_PREFERENCE,
        HEALTH_CONDITION
    }

    private data class DietarySection(
        val title: String,
        val items: List<DietType>,
        val sectionType: DietarySectionType
    )

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var currentMeasureData: UserMeasureData? = null
    private var diseaseLevels: List<DiseaseLevel> = emptyList()
    private var diseases: List<Disease> = emptyList()
    private var isUpdatingUI = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupButtons()

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_my_account -> showAccountInfo()
                    R.id.btn_measurements -> showMeasurements()
                    R.id.btn_dietary -> showDietary()
                    R.id.btn_diseases -> showDiseases()
                }
            }
        }

        setupMeasurementClickListeners()
        
        if (binding.toggleGroup.checkedButtonId == R.id.btn_my_account) {
            showAccountInfo()
        } else if (binding.toggleGroup.checkedButtonId == R.id.btn_measurements) {
            showMeasurements()
        } else if (binding.toggleGroup.checkedButtonId == R.id.btn_diseases) {
            showDiseases()
        } else {
            showDietary()
        }
    }

    private fun showAccountInfo() {
        binding.layoutAccountInfo.visibility = View.VISIBLE
        binding.layoutMeasurements.visibility = View.GONE
        binding.layoutDietary.visibility = View.GONE
        binding.layoutDiseases.visibility = View.GONE
        binding.btnLogout.visibility = View.VISIBLE
        binding.btnDeleteAccount.visibility = View.VISIBLE
        fetchUserAccount()
    }

    private fun showMeasurements() {
        binding.layoutAccountInfo.visibility = View.GONE
        binding.layoutMeasurements.visibility = View.VISIBLE
        binding.layoutDietary.visibility = View.GONE
        binding.layoutDiseases.visibility = View.GONE
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE
        fetchUserMeasurements()
    }

    private fun showDietary() {
        binding.layoutAccountInfo.visibility = View.GONE
        binding.layoutMeasurements.visibility = View.GONE
        binding.layoutDietary.visibility = View.VISIBLE
        binding.layoutDiseases.visibility = View.GONE
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE
        fetchDietTypes()
    }

    private fun showDiseases() {
        binding.layoutAccountInfo.visibility = View.GONE
        binding.layoutMeasurements.visibility = View.GONE
        binding.layoutDietary.visibility = View.GONE
        binding.layoutDiseases.visibility = View.VISIBLE
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE
        fetchDiseases()
    }

    private fun setupMeasurementClickListeners() {
        binding.rowGender.setOnClickListener {
            showPickerSheet("I am a", arrayOf("Guy", "Gal", "Prefer not to say")) { selected ->
                updateSingleField { it.copy(gender = selected) }
            }
        }

        binding.rowBirth.setOnClickListener {
            showDatePickerSheet { date ->
                updateSingleField { it.copy(birthDate = date, age = calculateAge(date)) }
            }
        }

        binding.rowHeight.setOnClickListener {
            showEditInputSheet("My height is", "cm") { value ->
                val h = value.toIntOrNull() ?: 0
                updateSingleField { 
                    val newBmi = calculateBmi(h, it.weight)
                    it.copy(height = h, bmi = newBmi) 
                }
            }
        }

        binding.rowWeight.setOnClickListener {
            showEditInputSheet("My current weight is", "kg") { value ->
                val w = value.toIntOrNull() ?: 0
                updateSingleField { 
                    val newBmi = calculateBmi(it.height, w)
                    it.copy(weight = w, bmi = newBmi) 
                }
            }
        }

        binding.switchBloodPressure.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUI) {
                updateSingleField { it.copy(bloodPressure = if (isChecked) 1 else 0) }
            }
        }

        binding.switchCholesterol.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUI) {
                updateSingleField { it.copy(cholesterol = if (isChecked) 1 else 0) }
            }
        }
    }

    private fun fetchUserAccount() {
        val token = sessionManager.fetchAuthToken() ?: return

        lifecycleScope.launch {
            try {
                val response = NetworkClient.userApiService.getUserAccount(token)
                if (response.success && response.data != null) {
                    displayAccountData(response.data)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayAccountData(data: UserAccountData) {
        binding.tvProfileEmail.text = data.email
        binding.tvProfileUsername.text = data.username ?: data.email.substringBefore("@")
    }

    private fun fetchUserMeasurements() {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                val response = NetworkClient.userApiService.getMeasure("Bearer $token")
                if (response.success && response.data != null) {
                    currentMeasureData = response.data
                    displayMeasureData(response.data)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching measurements: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMeasureData(data: UserMeasureData) {
        isUpdatingUI = true
        binding.tvGenderValue.text = data.gender ?: "please select"
        binding.tvBirthValue.text = data.birthDate ?: "not set"
        binding.tvHeightValue.text = "${data.height ?: 0} cm"
        binding.tvWeightValue.text = "${data.weight ?: 0} kg"
        binding.tvBmiValue.text = String.format("%.1f", data.bmi?.toDouble() ?: 0.0)
        
        binding.switchBloodPressure.isChecked = (data.bloodPressure ?: 0) == 1
        binding.switchCholesterol.isChecked = (data.cholesterol ?: 0) == 1
        isUpdatingUI = false
    }

    private fun fetchDietTypes() {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            val sections = mutableListOf<DietarySection>()

            try {
                val dietTypesResponse = NetworkClient.userApiService.getDietTypes(token)
                if (dietTypesResponse.success && !dietTypesResponse.data.isNullOrEmpty()) {
                    val groupedDietTypes = dietTypesResponse.data.groupBy { it.category ?: "My Diet" }
                    groupedDietTypes.forEach { (category, items) ->
                        sections.add(
                            DietarySection(
                                title = category,
                                items = items,
                                sectionType = DietarySectionType.DIET_TYPE
                            )
                        )
                    }
                }
            } catch (_: Exception) {
            }

            try {
                val dietaryPreferencesResponse = NetworkClient.userApiService.getDietaryPreferences(token)
                if (dietaryPreferencesResponse.success && !dietaryPreferencesResponse.data.isNullOrEmpty()) {
                    sections.add(
                        DietarySection(
                            title = "Dietary Preferences",
                            items = dietaryPreferencesResponse.data,
                            sectionType = DietarySectionType.DIETARY_PREFERENCE
                        )
                    )
                }
            } catch (_: Exception) {
            }

            try {
                val healthConditionsResponse = NetworkClient.userApiService.getHealthConditions(token)
                if (healthConditionsResponse.success && !healthConditionsResponse.data.isNullOrEmpty()) {
                    sections.add(
                        DietarySection(
                            title = "Health Condition",
                            items = healthConditionsResponse.data,
                            sectionType = DietarySectionType.HEALTH_CONDITION
                        )
                    )
                }
            } catch (_: Exception) {
            }

            if (sections.isNotEmpty()) {
                populateDietaryUI(sections)
            } else {
                Toast.makeText(context, "Error fetching dietary data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDiseases() {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            diseases = emptyList()
            diseaseLevels = emptyList()

            try {
                val diseaseLevelsResponse = NetworkClient.userApiService.getDiseaseLevels(token)
                if (diseaseLevelsResponse.success && !diseaseLevelsResponse.data.isNullOrEmpty()) {
                    diseaseLevels = diseaseLevelsResponse.data
                }
            } catch (_: Exception) {
            }

            try {
                val diseasesResponse = NetworkClient.userApiService.getDiseases(token)
                if (diseasesResponse.success && !diseasesResponse.data.isNullOrEmpty()) {
                    diseases = diseasesResponse.data
                }
            } catch (_: Exception) {
            }

            if (diseases.isNotEmpty()) {
                populateDiseasesUI(diseases)
            } else {
                Toast.makeText(context, "Error fetching diseases", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateDietaryUI(sections: List<DietarySection>) {
        binding.dietaryItemsContainer.removeAllViews()

        for (section in sections) {
            addSectionHeader(binding.dietaryItemsContainer, section.title, "Personalized picks")

            for (item in section.items) {
                val row = createDietRow(item, section.sectionType)
                binding.dietaryItemsContainer.addView(row)
            }
        }
    }

    private fun populateDiseasesUI(items: List<Disease>) {
        binding.diseaseItemsContainer.removeAllViews()
        addSectionHeader(binding.diseaseItemsContainer, "Diseases", "Tap a card to set level")

        items.forEach { disease ->
            binding.diseaseItemsContainer.addView(createDiseaseRow(disease))
        }
    }

    private fun addSectionHeader(container: LinearLayout, title: String, description: String) {
        val titleView = TextView(context).apply {
            text = title
            textSize = 18f
            setPadding(0, dp(24), 0, dp(8))
            setTextColor(resources.getColor(R.color.black, null))
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(titleView)

        val descView = TextView(context).apply {
            text = description
            textSize = 12f
            setPadding(0, 0, 0, dp(16))
            setTextColor(resources.getColor(R.color.black, null))
            alpha = 0.5f
        }
        container.addView(descView)
    }

    private fun createDietRow(item: DietType, sectionType: DietarySectionType): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(android.graphics.Color.parseColor("#F7F9FC"))
                setStroke(dp(1), android.graphics.Color.parseColor("#E7EDF7"))
            }
        }

        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameView = TextView(context).apply {
            text = item.name
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(R.color.black, null))
        }

        val subtitleView = TextView(context).apply {
            text = when (sectionType) {
                DietarySectionType.DIET_TYPE -> "Diet"
                DietarySectionType.DIETARY_PREFERENCE -> "Preference"
                DietarySectionType.HEALTH_CONDITION -> "Condition"
            }
            textSize = 12f
            setPadding(0, dp(4), 0, 0)
            setTextColor(android.graphics.Color.parseColor("#7B8494"))
        }

        val switch = SwitchMaterial(requireContext()).apply {
            isChecked = item.isActive
            setOnCheckedChangeListener { _, isChecked ->
                updateDietary(item.id, isChecked, sectionType)
            }
        }

        textColumn.addView(nameView)
        textColumn.addView(subtitleView)
        layout.addView(textColumn)
        layout.addView(switch)

        return layout
    }

    private fun createDiseaseRow(disease: Disease): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(android.graphics.Color.parseColor("#F8FAFD"))
                setStroke(dp(1), android.graphics.Color.parseColor("#DDE7F5"))
            }
            isClickable = true
            isFocusable = true
            foreground = requireContext().getDrawable(android.R.drawable.list_selector_background)
            setOnClickListener {
                showDiseaseLevelSheet(disease)
            }
        }

        val titleRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleView = TextView(context).apply {
            text = disease.name
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val levelBadge = TextView(context).apply {
            text = disease.diseaseLevel?.name ?: "Not Selected"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#2F6BFF"))
            setPadding(dp(12), dp(6), dp(12), dp(6))
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = dp(50).toFloat()
                setColor(android.graphics.Color.parseColor("#E8F0FF"))
            }
        }

        val descriptionView = TextView(context).apply {
            text = disease.description ?: "Set activity and level"
            textSize = 13f
            maxLines = 2
            setPadding(0, dp(10), 0, 0)
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
        }

        val footerRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, 0)
        }

        val statusView = TextView(context).apply {
            text = buildDiseaseStatusText(disease)
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val actionView = TextView(context).apply {
            text = "Manage"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(R.color.blue, null))
        }

        titleRow.addView(titleView)
        titleRow.addView(levelBadge)
        footerRow.addView(statusView)
        footerRow.addView(actionView)

        card.addView(titleRow)
        card.addView(descriptionView)
        card.addView(footerRow)

        return card
    }

    private fun buildDiseaseStatusText(disease: Disease): String {
        val codeText = disease.code?.takeIf { it.isNotBlank() }?.let { "Code $it" } ?: "Condition"
        val state = if (disease.isActive) "Active" else "Inactive"
        return "$codeText • $state"
    }

    private fun updateDietary(id: Int, isActive: Boolean, sectionType: DietarySectionType) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                val response = when (sectionType) {
                    DietarySectionType.DIET_TYPE -> NetworkClient.userApiService.updateDietType(token, id, UpdateDietTypeRequest(isActive))
                    DietarySectionType.DIETARY_PREFERENCE -> NetworkClient.userApiService.updateDietaryPreference(token, id, UpdateDietTypeRequest(isActive))
                    DietarySectionType.HEALTH_CONDITION -> NetworkClient.userApiService.updateHealthCondition(token, id, UpdateDietTypeRequest(isActive))
                }
                if (response.success) {
                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, response.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDiseaseLevelSheet(disease: Disease) {
        if (diseaseLevels.isEmpty()) {
            Toast.makeText(context, "Disease levels are not available", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)
        val root = view as LinearLayout
        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.tv_done)
        val btnCancel = view.findViewById<TextView>(R.id.tv_cancel)

        val contentContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(4))
        }

        val titleView = TextView(context).apply {
            text = disease.name
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(R.color.black, null))
        }

        val descriptionView = TextView(context).apply {
            text = disease.code?.takeIf { it.isNotBlank() }?.let { "Code $it" } ?: "Set activity and level"
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
            setPadding(0, dp(8), 0, dp(16))
        }

        val activeSwitch = SwitchMaterial(requireContext()).apply {
            text = "This condition is active"
            isChecked = disease.isActive
            textSize = 14f
            setTextColor(resources.getColor(R.color.black, null))
        }

        val helperView = TextView(context).apply {
            text = "Severity level"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
            setPadding(0, dp(18), 0, dp(8))
        }

        val pickerValues = diseaseLevels.map { it.name }.toTypedArray()
        numberPicker.minValue = 0
        numberPicker.maxValue = pickerValues.size - 1
        numberPicker.displayedValues = pickerValues
        numberPicker.wrapSelectorWheel = false
        numberPicker.value = diseaseLevels.indexOfFirst { it.id == disease.diseaseLevel?.id }.takeIf { it >= 0 } ?: 0

        contentContainer.addView(titleView)
        contentContainer.addView(descriptionView)
        contentContainer.addView(activeSwitch)
        contentContainer.addView(helperView)
        root.addView(contentContainer, 2)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDone.text = "Save"
        btnDone.setOnClickListener {
            val selectedLevel = diseaseLevels[numberPicker.value]
            updateDisease(disease.id, activeSwitch.isChecked, selectedLevel.id, dialog)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateDisease(diseaseId: Int, isActive: Boolean, diseaseLevelId: Int, dialog: BottomSheetDialog) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                val response = NetworkClient.userApiService.updateDisease(
                    token,
                    diseaseId,
                    UpdateDiseaseRequest(diseaseLevelId = diseaseLevelId, isActive = isActive)
                )
                if (response.success) {
                    dialog.dismiss()
                    fetchDietTypes()
                    Toast.makeText(context, "Disease updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, response.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSingleField(updateBlock: (UpdateUserMeasureRequest) -> UpdateUserMeasureRequest) {
        val data = currentMeasureData ?: return
        val token = sessionManager.fetchAuthToken() ?: return

        val currentRequest = UpdateUserMeasureRequest(
            age = data.age ?: "0",
            birthDate = data.birthDate ?: "2000-01-01",
            bloodPressure = data.bloodPressure ?: 0,
            bmi = data.bmi ?: 0,
            cholesterol = data.cholesterol ?: 0,
            dailyCaloriesGoal = data.dailyCaloriesGoal ?: 0,
            dailyWaterGoal = data.dailyWaterGoal ?: 0,
            gender = data.gender ?: "string",
            height = data.height ?: 0,
            weight = data.weight ?: 0
        )

        val updatedRequest = updateBlock(currentRequest)

        lifecycleScope.launch {
            try {
                val response = NetworkClient.userApiService.updateMeasure("Bearer $token", updatedRequest)
                if (response.success && response.data != null) {
                    currentMeasureData = response.data
                    displayMeasureData(response.data)
                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, response.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                currentMeasureData?.let { displayMeasureData(it) }
            }
        }
    }

    private fun calculateBmi(heightCm: Int, weightKg: Int): Int {
        if (heightCm == 0) return 0
        val heightM = heightCm / 100.0
        return (weightKg / (heightM * heightM)).toInt()
    }

    private fun calculateAge(birthDate: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(birthDate) ?: return "0"
            val dob = Calendar.getInstance()
            dob.time = date
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age.toString()
        } catch (e: Exception) {
            "0"
        }
    }

    private fun setupButtons() {
        binding.btnLogout.setOnClickListener {
            sessionManager.clearData()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        binding.btnDeleteAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Delete feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPickerSheet(title: String, options: Array<String>, onSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val picker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.tv_done)
        val btnCancel = view.findViewById<TextView>(R.id.tv_cancel)

        picker.minValue = 0
        picker.maxValue = options.size - 1
        picker.displayedValues = options
        picker.wrapSelectorWheel = false

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDone.setOnClickListener {
            onSelected(options[picker.value])
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDatePickerSheet(onDateSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val datePicker = view.findViewById<DatePicker>(R.id.date_picker)
        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.tv_done)
        val btnCancel = view.findViewById<TextView>(R.id.tv_cancel)

        numberPicker.visibility = View.GONE
        datePicker.visibility = View.VISIBLE

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDone.setOnClickListener {
            val dateString = String.format("%04d-%02d-%02d", datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
            onDateSelected(dateString)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showEditInputSheet(title: String, unit: String, onValueEntered: (String) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val container = view.findViewById<NumberPicker>(R.id.number_picker).parent as LinearLayout
        view.findViewById<NumberPicker>(R.id.number_picker).visibility = View.GONE

        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter value"
            textSize = 24f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(40, 40, 40, 40) }
        }
        container.addView(input)

        view.findViewById<TextView>(R.id.tv_done).setOnClickListener {
            val value = input.text.toString()
            if (value.isNotEmpty()) {
                onValueEntered(value)
            }
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.tv_cancel).setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
