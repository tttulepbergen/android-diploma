package com.example.scanfit.mainNavigation.user.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentProfileBinding
import com.example.scanfit.model.UpdateUserMeasureRequest
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        binding.etBirthdate.setOnClickListener { showDatePicker() }
        binding.btnContinue.setOnClickListener { validateAndContinue() }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select your birthdate")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            binding.etBirthdate.setText(formatter.format(Date(selection)))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun validateAndContinue() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val height = binding.etHeightCm.text.toString().trim().toIntOrNull()
        val weight = binding.etWeight.text.toString().trim().toIntOrNull()
        val birthdateInput = binding.etBirthdate.text.toString().trim()
        val birthdate = birthdateInput.toApiDateOrNull()

        if (height == null || weight == null || birthdate == null) {
            Toast.makeText(requireContext(), "Please fill height, weight, and birthdate", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when (binding.toggleGender.checkedButtonId) {
            R.id.btn_female -> "Gal"
            R.id.btn_male -> "Guy"
            else -> "Prefer not to say"
        }

        val request = UpdateUserMeasureRequest(
            age = calculateAge(birthdate),
            birthDate = birthdate,
            bloodPressure = null,
            cholesterol = null,
            gender = gender,
            height = height,
            weight = weight
        )

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = NetworkClient.userApiService.updateMeasure("Bearer $token", request)
                if (response.success) {
                    val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val userId = sessionManager.fetchUserId()
                    prefs.edit().apply {
                        putString("user_height_$userId", height.toString())
                        putString("user_weight_$userId", weight.toString())
                        putString("user_gender_$userId", gender)
                        putString("user_birthdate_$userId", birthdateInput)
                        apply()
                    }

                    findNavController().navigate(
                        R.id.action_profileFragment2_to_dietSelectionFragment,
                        bundleOf("setup_weight" to weight)
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "Failed to save profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Failed to save profile",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.blockingLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnContinue.isEnabled = !isLoading
    }

    private fun String.toApiDateOrNull(): String? {
        return runCatching {
            val source = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val target = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            target.format(source.parse(this) ?: return null)
        }.getOrNull()
    }

    private fun calculateAge(apiBirthDate: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val birth = sdf.parse(apiBirthDate) ?: return "0"
            val dob = Calendar.getInstance().apply { time = birth }
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age.toString()
        } catch (_: Exception) {
            "0"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
