package com.example.scanfit.mainNavigation.user.profile

import android.app.AlertDialog
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
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentUserProfileBinding
import com.example.scanfit.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        // Note: Firebase user info removed as we switched to custom backend
        // In a real app, you'd fetch user info from the backend using the token
        binding.tvProfileEmail.text = "User" 
        binding.tvProfileUsername.text = "Account"

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupButtons()

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_my_account -> showAccountInfo()
                    R.id.btn_measurements -> showMeasurements()
                }
            }
        }

        loadUserMeasurements()
    }

    private fun showAccountInfo() {
        binding.layoutAccountInfo.visibility = View.VISIBLE
        binding.layoutMeasurements.visibility = View.GONE
        binding.btnLogout.visibility = View.VISIBLE
        binding.btnDeleteAccount.visibility = View.VISIBLE
    }

    private fun showMeasurements() {
        binding.layoutAccountInfo.visibility = View.GONE
        binding.layoutMeasurements.visibility = View.VISIBLE
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE

        loadUserMeasurements()
        setupMeasurementClickListeners()
    }

    private fun setupMeasurementClickListeners() {
        binding.rowGender.setOnClickListener {
            showPickerSheet("I am a", arrayOf("Gal", "Guy", "Prefer not to say"), "user_gender", binding.tvGenderValue)
        }

        binding.rowBirth.setOnClickListener {
            showDatePickerSheet()
        }

        binding.rowHeight.setOnClickListener {
            showEditInputSheet("My height is", "cm", "user_height", binding.tvHeightValue)
        }

        binding.rowWeight.setOnClickListener {
            showEditInputSheet("My current weight is", "kg", "user_weight", binding.tvWeightValue)
        }
    }

    private fun loadUserMeasurements() {
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        binding.tvHeightValue.text = prefs.getString("user_height", "not set")
        binding.tvWeightValue.text = prefs.getString("user_weight", "not set")
        binding.tvGenderValue.text = prefs.getString("user_gender", "please select")
        binding.tvBirthValue.text = prefs.getString("user_birth", "not set")
    }

    private fun saveData(key: String, value: String) {
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    private fun setupButtons() {
        binding.btnLogout.setOnClickListener {
            sessionManager.clearData()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            // Optionally restart the app or navigate to login
        }

        binding.btnDeleteAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Delete feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPickerSheet(title: String, options: Array<String>, key: String, textView: TextView) {
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
            val selectedValue = options[picker.value]
            textView.text = selectedValue
            saveData(key, selectedValue)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDatePickerSheet() {
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
            val dateString = "${datePicker.dayOfMonth}.${datePicker.month + 1}.${datePicker.year}"
            binding.tvBirthValue.text = dateString
            saveData("user_birth", dateString)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showEditInputSheet(title: String, unit: String, key: String, textView: TextView) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val container = view.findViewById<NumberPicker>(R.id.number_picker).parent as LinearLayout
        view.findViewById<NumberPicker>(R.id.number_picker).visibility = View.GONE

        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Enter value"
            textSize = 24f
            gravity = Gravity.CENTER
        }
        container.addView(input)

        view.findViewById<TextView>(R.id.tv_done).setOnClickListener {
            val value = input.text.toString()
            if (value.isNotEmpty()) {
                val result = "$value $unit"
                textView.text = result
                saveData(key, result)
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