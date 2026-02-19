package com.example.scanfit.mainNavigation.user.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scanfit.databinding.FragmentProfileBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import com.example.scanfit.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        binding.etBirthdate.setOnClickListener {
            showDatePicker()
        }

        binding.btnContinue.setOnClickListener {
            validateAndContinue()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select your birthdate")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateString = formatter.format(Date(selection))
            binding.etBirthdate.setText(dateString)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }


    private fun validateAndContinue() {
        val height = binding.etHeightCm.text.toString()
        val weight = binding.etWeight.text.toString()
        val birthdate = binding.etBirthdate.text.toString()

        val checkedId = binding.toggleGender.checkedButtonId
        val gender = when (checkedId) {
            R.id.btn_female -> "Gal"
            R.id.btn_male -> "Guy"
            else -> "Prefer not to say"
        }

        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (height.isNotEmpty()) putString("user_height", height)
            if (weight.isNotEmpty()) putString("user_weight", weight)
            if (birthdate.isNotEmpty()) putString("user_birthdate", birthdate)
            putString("user_gender", gender)
            apply()
        }

        findNavController().navigate(R.id.action_profileFragment2_to_dietSelectionFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
