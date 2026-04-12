package com.example.scanfit.mainNavigation.user.profile

import android.content.Context
import android.os.Bundle
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.scanfit.R
import com.example.scanfit.adapters.DietAdapter
import com.example.scanfit.data.DietItem
import com.example.scanfit.databinding.FragmentDietSelectionBinding
import com.example.scanfit.model.DietType
import com.example.scanfit.model.Disease
import com.example.scanfit.model.DiseaseLevel
import com.example.scanfit.model.UpdateDietTypeRequest
import com.example.scanfit.model.UpdateDiseaseRequest
import com.example.scanfit.model.UpdateRegistrationStatusRequest
import com.example.scanfit.model.UpdateWeightManagementRequest
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.navigation.fragment.findNavController

class DietSelectionFragment : Fragment(R.layout.fragment_diet_selection) {

    private var _binding: FragmentDietSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val MAX_ACTIVE_DISEASES = 3
    }

    private var diseases: MutableList<Disease> = mutableListOf()
    private var dietTypes: MutableList<DietType> = mutableListOf()
    private var diseaseLevels: List<DiseaseLevel> = emptyList()
    private var setupWeight: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDietSelectionBinding.bind(view)
        sessionManager = SessionManager(requireContext())
        setupWeight = arguments?.getInt("setup_weight") ?: 0

        binding.rvDiets.layoutManager = FlexboxLayoutManager(context)
        binding.btnFinishSetup.setOnClickListener { saveAllCategories() }

        loadSetupOptions()
    }

    private fun loadSetupOptions() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            showError("User token not found")
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val diseaseLevelsResponse = NetworkClient.userApiService.getDiseaseLevels(token)
                if (diseaseLevelsResponse.success) {
                    diseaseLevels = diseaseLevelsResponse.data.orEmpty()
                }

                val diseasesResponse = NetworkClient.userApiService.getDiseases(token)
                if (diseasesResponse.success) {
                    diseases = diseasesResponse.data.orEmpty().toMutableList()
                }

                val dietTypesResponse = NetworkClient.userApiService.getDietTypes(token)
                if (dietTypesResponse.success) {
                    dietTypes = dietTypesResponse.data.orEmpty().toMutableList()
                }

                updateRecyclerView()
            } catch (e: Exception) {
                showError(e.message ?: "Failed to load setup options")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun updateRecyclerView() {
        val displayList = mutableListOf<Any>()
        if (dietTypes.isNotEmpty()) {
            displayList.add("Diet Types")
            displayList.addAll(dietTypes.map { it.toDietItem("diet_type") })
        }
        if (diseases.isNotEmpty()) {
            displayList.add("Diseases")
            displayList.addAll(diseases.map { it.toDietItem() })
        }

        if (binding.rvDiets.adapter == null) {
            binding.rvDiets.adapter = DietAdapter(displayList) { diet ->
                toggleSelection(diet)
            }
        } else {
            (binding.rvDiets.adapter as DietAdapter).updateData(displayList)
        }
    }

    private fun toggleSelection(item: DietItem) {
        val parts = item.id.split(":", limit = 2)
        if (parts.size != 2) return
        val type = parts[0]
        val id = parts[1]

        when (type) {
            "diet_type" -> {
                dietTypes = dietTypes.map {
                    it.copy(isActive = it.id.toString() == id)
                }.toMutableList()
            }
            "disease" -> {
                val disease = diseases.firstOrNull { it.id.toString() == id } ?: return
                showDiseaseLevelSheet(disease)
                return
            }
        }

        updateRecyclerView()
    }

    private fun saveAllCategories() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            showError("User token not found")
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                runSaveRequests(token)

                val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val userId = sessionManager.fetchUserId()
                prefs.edit().apply {
                    putBoolean("profile_completed", true)
                    putBoolean("profile_completed_$userId", true)
                    apply()
                }

                findNavController().navigate(R.id.action_dietSelectionFragment_to_homeFragment)
            } catch (e: Exception) {
                showError(e.message ?: "Failed to finish setup")
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun runSaveRequests(token: String) {
        dietTypes.forEach { item ->
            val response = NetworkClient.userApiService.updateDietType(
                token,
                item.id,
                UpdateDietTypeRequest(isActive = item.isActive)
            )
            if (!response.success) error(response.message ?: "Failed to save diet type")
        }

        val defaultDiseaseLevelId = diseaseLevels.firstOrNull()?.id ?: 1
        diseases.forEach { disease ->
            val response = NetworkClient.userApiService.updateDisease(
                token,
                disease.id,
                UpdateDiseaseRequest(
                    diseaseLevelId = disease.diseaseLevel?.id ?: defaultDiseaseLevelId,
                    isActive = disease.isActive
                )
            )
            if (!response.success) error(response.message ?: "Failed to save disease")
        }

        val weightResponse = NetworkClient.userApiService.updateWeightManagement(
            token,
            UpdateWeightManagementRequest(
                goal = "Maintain",
                targetDate = buildDefaultTargetDate(),
                targetWeight = setupWeight.takeIf { it > 0 },
                weeklyWeightChange = 0
            )
        )
        if (!weightResponse.success) error(weightResponse.message ?: "Failed to save weight management")

        NetworkClient.userApiService.getTodayUserCalories(token)
        NetworkClient.userApiService.getTodayUserWater(token)

        val registrationResponse = NetworkClient.userApiService.updateRegistrationStatus(
            token,
            UpdateRegistrationStatusRequest(isFinishedRegister = true)
        )
        if (!registrationResponse.success) error(registrationResponse.message ?: "Failed to update registration status")
    }

    private fun buildDefaultTargetDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
            Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.blockingLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnFinishSetup.isEnabled = !isLoading
    }

    private fun DietType.toDietItem(type: String): DietItem {
        return DietItem(
            id = "$type:$id",
            name = name,
            description = description?.takeIf { it.isNotBlank() },
            ui_type = "toggle",
            isSelected = isActive,
            category_name = type
        )
    }

    private fun Disease.toDietItem(): DietItem {
        val levelName = diseaseLevel?.name?.takeIf { it.isNotBlank() } ?: "Choose level"
        return DietItem(
            id = "disease:$id",
            name = "$name - $levelName",
            description = description?.takeIf { it.isNotBlank() } ?: "Tap to choose level and active state",
            ui_type = "toggle",
            isSelected = isActive,
            category_name = "disease"
        )
    }

    private fun showDiseaseLevelSheet(disease: Disease) {
        if (diseaseLevels.isEmpty()) {
            showError("Disease levels are not available")
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
            text = "Choose a level and mark this disease active if needed."
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#6B7280"))
            setPadding(0, dp(8), 0, dp(16))
        }

        val activeSwitch = SwitchMaterial(requireContext()).apply {
            text = "Active disease"
            isChecked = disease.isActive
            textSize = 14f
            setTextColor(resources.getColor(R.color.black, null))
        }

        val helperView = TextView(context).apply {
            text = "Disease level"
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
        numberPicker.value = diseaseLevels.indexOfFirst { it.id == disease.diseaseLevel?.id }
            .takeIf { it >= 0 } ?: 0

        contentContainer.addView(titleView)
        contentContainer.addView(descriptionView)
        contentContainer.addView(activeSwitch)
        contentContainer.addView(helperView)
        root.addView(contentContainer, 2)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDone.text = "Save"
        btnDone.setOnClickListener {
            val selectedLevel = diseaseLevels[numberPicker.value]
            applyDiseaseSelection(disease.id, activeSwitch.isChecked, selectedLevel)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun applyDiseaseSelection(diseaseId: Int, isActive: Boolean, diseaseLevel: DiseaseLevel) {
        if (isActive && exceedsDiseaseLimit(diseaseId)) {
            showError("You can choose up to $MAX_ACTIVE_DISEASES diseases")
            return
        }

        diseases = diseases.map { disease ->
            if (disease.id == diseaseId) {
                disease.copy(isActive = isActive, diseaseLevel = diseaseLevel)
            } else {
                disease
            }
        }.toMutableList()

        updateRecyclerView()
    }

    private fun exceedsDiseaseLimit(diseaseId: Int): Boolean {
        val alreadyActive = diseases.any { it.id == diseaseId && it.isActive }
        if (alreadyActive) return false
        return diseases.count { it.isActive } >= MAX_ACTIVE_DISEASES
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
