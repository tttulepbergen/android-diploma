package com.example.scanfit.mainNavigation.user.profile

import android.content.Context
import android.os.Bundle
import android.text.InputType
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
import com.example.scanfit.model.DietType
import com.example.scanfit.model.UpdateDietTypeRequest
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
                }
            }
        }

        setupMeasurementClickListeners()
        
        if (binding.toggleGroup.checkedButtonId == R.id.btn_my_account) {
            showAccountInfo()
        } else if (binding.toggleGroup.checkedButtonId == R.id.btn_measurements) {
            showMeasurements()
        } else {
            showDietary()
        }
    }

    private fun showAccountInfo() {
        binding.layoutAccountInfo.visibility = View.VISIBLE
        binding.layoutMeasurements.visibility = View.GONE
        binding.layoutDietary.visibility = View.GONE
        binding.btnLogout.visibility = View.VISIBLE
        binding.btnDeleteAccount.visibility = View.VISIBLE
        fetchUserAccount()
    }

    private fun showMeasurements() {
        binding.layoutAccountInfo.visibility = View.GONE
        binding.layoutMeasurements.visibility = View.VISIBLE
        binding.layoutDietary.visibility = View.GONE
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE
        fetchUserMeasurements()
    }

    private fun showDietary() {
        binding.layoutAccountInfo.visibility = View.GONE
        binding.layoutMeasurements.visibility = View.GONE
        binding.layoutDietary.visibility = View.VISIBLE
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE
        fetchDietTypes()
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

    private fun populateDietaryUI(sections: List<DietarySection>) {
        binding.dietaryItemsContainer.removeAllViews()

        for (section in sections) {
            val titleView = TextView(context).apply {
                text = section.title
                textSize = 18f
                setPadding(0, 40, 0, 8)
                setTextColor(resources.getColor(R.color.black, null))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            binding.dietaryItemsContainer.addView(titleView)
            
            val descView = TextView(context).apply {
                text = "(Premium feature - Scan&Fit Pro)"
                textSize = 12f
                setPadding(0, 0, 0, 16)
                setTextColor(resources.getColor(R.color.black, null))
                alpha = 0.5f
            }
            binding.dietaryItemsContainer.addView(descView)

            for (item in section.items) {
                val row = createDietRow(item, section.sectionType)
                binding.dietaryItemsContainer.addView(row)
            }
        }
    }

    private fun createDietRow(item: DietType, sectionType: DietarySectionType): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                110 // height in pixels approx 55dp
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val nameView = TextView(context).apply {
            text = item.name
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setTextColor(resources.getColor(R.color.black, null))
        }

        val switch = SwitchMaterial(requireContext()).apply {
            isChecked = item.isActive
            setOnCheckedChangeListener { _, isChecked ->
                updateDietary(item.id, isChecked, sectionType)
            }
        }

        layout.addView(nameView)
        layout.addView(switch)
        
        return layout
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
