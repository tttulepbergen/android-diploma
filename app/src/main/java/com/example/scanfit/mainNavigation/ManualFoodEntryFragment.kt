package com.example.scanfit.mainNavigation

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentManualFoodEntryBinding
import com.example.scanfit.model.CreateUserDailyEatRequest
import com.example.scanfit.model.UpdateUserCaloriesRequest
import com.example.scanfit.model.UserCaloriesData
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManualFoodEntryFragment : Fragment(R.layout.fragment_manual_food_entry) {

    private var _binding: FragmentManualFoodEntryBinding? = null
    private val binding get() = _binding!!
    private val trackerViewModel: TrackerViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null
    private val isPro get() = if (::sessionManager.isInitialized) sessionManager.isVip() else false

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivFoodPhoto.load(uri) { crossfade(true) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            _binding = FragmentManualFoodEntryBinding.bind(view)
            sessionManager = SessionManager(requireContext())

            if (isPro) {
                binding.layoutProFields.visibility = View.VISIBLE
                binding.layoutProLocked.visibility = View.GONE
            } else {
                binding.layoutProFields.visibility = View.GONE
                binding.layoutProLocked.visibility = View.VISIBLE
            }

            binding.btnBack.setOnClickListener {
                try {
                    findNavController().navigateUp()
                } catch (e: Exception) {
                    Log.e("ManualFoodEntry", "Back navigation failed", e)
                }
            }
            binding.btnPickPhoto.setOnClickListener { pickImage.launch("image/*") }
            binding.btnSave.setOnClickListener { submit() }
        } catch (e: Exception) {
            Log.e("ManualFoodEntry", "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading screen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun submit() {
        val name = binding.etName.text?.toString()?.trim()
        if (name.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please enter a product name", Toast.LENGTH_SHORT).show()
            return
        }

        val calories = binding.etCalories.text?.toString()?.toDoubleOrNull() ?: 0.0
        val proteins = binding.etProteins.text?.toString()?.toDoubleOrNull() ?: 0.0
        val fats = binding.etFats.text?.toString()?.toDoubleOrNull() ?: 0.0
        val carbs = binding.etCarbs.text?.toString()?.toDoubleOrNull() ?: 0.0

        val sugars = if (isPro) binding.etSugars.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val fiber = if (isPro) binding.etFiber.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val sodium = if (isPro) binding.etSodium.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val cholesterol = if (isPro) binding.etCholesterol.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val vitaminD = if (isPro) binding.etVitaminD.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val vitaminB12 = if (isPro) binding.etVitaminB12.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val vitaminC = if (isPro) binding.etVitaminC.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val vitaminA = if (isPro) binding.etVitaminA.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val vitaminB6 = if (isPro) binding.etVitaminB6.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val vitaminB9 = if (isPro) binding.etVitaminB9.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0
        val vitaminE = if (isPro) binding.etVitaminE.text?.toString()?.toDoubleOrNull() ?: 0.0 else 0.0

        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false

        val selectedDate = trackerViewModel.selectedDate.value ?: Calendar.getInstance()
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)

        val dailyEatRequest = CreateUserDailyEatRequest(
            calorie = calories.toInt(),
            carbohydrate = carbs.toInt(),
            cholesterol = cholesterol.toInt(),
            fats = fats.toInt(),
            fiber = fiber.toInt(),
            portion = 1.0,
            productName = name,
            protein = proteins.toInt(),
            sodium = sodium.toInt(),
            sugar = sugars.toInt(),
            vitaminA = vitaminA,
            vitaminB12 = vitaminB12,
            vitaminB6 = vitaminB6,
            vitaminB9 = vitaminB9,
            vitaminC = vitaminC,
            vitaminD = vitaminD,
            vitaminE = vitaminE
        )

        val caloriesRequest = UpdateUserCaloriesRequest(
            calories = calories.toInt(),
            carbs = carbs.toInt(),
            fat = fats.toInt(),
            proteins = proteins.toInt(),
            fiber = if (isPro) fiber.toInt() else null,
            sodium = if (isPro) sodium.toInt() else null,
            sugar = if (isPro) sugars.toInt() else null,
            cholesterol = if (isPro) cholesterol.toInt() else null,
            vitaminA = if (isPro) vitaminA else null,
            vitaminB12 = if (isPro) vitaminB12 else null,
            vitaminB6 = if (isPro) vitaminB6 else null,
            vitaminB9 = if (isPro) vitaminB9 else null,
            vitaminC = if (isPro) vitaminC else null,
            vitaminD = if (isPro) vitaminD else null,
            vitaminE = if (isPro) vitaminE else null
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val createResponse = NetworkClient.userApiService.createUserDailyEat(token, dailyEatRequest)
                if (createResponse.success == false) {
                    Toast.makeText(
                        requireContext(),
                        createResponse.message ?: "Failed to add food",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSave.isEnabled = true
                    return@launch
                }

                val caloriesResponse = NetworkClient.userApiService.updateUserCalories(token, day, caloriesRequest)
                if (caloriesResponse.success && caloriesResponse.data != null) {
                    applyUpdatedCalories(caloriesResponse.data)
                    Toast.makeText(requireContext(), "$name added!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(
                        requireContext(),
                        caloriesResponse.message ?: "Failed to update calories",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("ManualFoodEntry", "Failed to add food", e)
                Toast.makeText(requireContext(), e.message ?: "Error adding food", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
