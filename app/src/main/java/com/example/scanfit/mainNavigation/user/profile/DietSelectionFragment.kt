package com.example.scanfit.mainNavigation.user.profile

import android.content.Context
import android.os.Bundle
import android.view.View
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.navigation.fragment.findNavController

class DietSelectionFragment : Fragment(R.layout.fragment_diet_selection) {

    private var _binding: FragmentDietSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private var diseases: MutableList<Disease> = mutableListOf()
    private var dietaryPreferences: MutableList<DietType> = mutableListOf()
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

                val dietaryPreferencesResponse = NetworkClient.userApiService.getDietaryPreferences(token)
                if (dietaryPreferencesResponse.success) {
                    dietaryPreferences = dietaryPreferencesResponse.data.orEmpty().toMutableList()
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
        if (dietaryPreferences.isNotEmpty()) {
            displayList.add("Dietary Preferences")
            displayList.addAll(dietaryPreferences.map { it.toDietItem("dietary_preference") })
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
                    if (it.id.toString() == id) it.copy(isActive = !it.isActive) else it
                }.toMutableList()
            }
            "dietary_preference" -> {
                dietaryPreferences = dietaryPreferences.map {
                    if (it.id.toString() == id) it.copy(isActive = !it.isActive) else it
                }.toMutableList()
            }
            "disease" -> {
                diseases = diseases.map {
                    if (it.id.toString() == id) it.copy(isActive = !it.isActive) else it
                }.toMutableList()
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

        dietaryPreferences.forEach { item ->
            val response = NetworkClient.userApiService.updateDietaryPreference(
                token,
                item.id,
                UpdateDietTypeRequest(isActive = item.isActive)
            )
            if (!response.success) error(response.message ?: "Failed to save dietary preference")
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
            ui_type = "toggle",
            isSelected = isActive,
            category_name = type
        )
    }

    private fun Disease.toDietItem(): DietItem {
        return DietItem(
            id = "disease:$id",
            name = name,
            ui_type = "toggle",
            isSelected = isActive,
            category_name = "disease"
        )
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
