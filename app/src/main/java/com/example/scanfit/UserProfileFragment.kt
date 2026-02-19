package com.example.scanfit

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scanfit.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            binding.tvProfileEmail.text = it.email
            binding.tvProfileUsername.text = it.displayName ?: it.email?.substringBefore("@") ?: "User"
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupButtons()

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
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
    }

    private fun saveData(key: String, value: String) {
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }


    private fun setupButtons() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()

            requireActivity().finish()
        }

        binding.btnDeleteAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Delete feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGenderPicker() {
        val options = arrayOf("Gal", "Guy", "Prefer not to say")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Gender")
            .setItems(options) { _, which ->
                val selected = options[which]
                binding.tvGenderValue.text = selected
                saveData("user_gender", selected)
            }
            .show()
    }

    private fun showWeightInputDialog() {
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setPadding(50, 40, 50, 40)

        AlertDialog.Builder(requireContext())
            .setTitle("My current weight is")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newWeight = input.text.toString()
                if (newWeight.isNotEmpty()) {
                    binding.tvWeightValue.text = "$newWeight kg"
                    saveData("user_weight", "$newWeight kg")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHeightPicker() {
        val heights = (140..220).map { "$it cm" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("My height is")
            .setItems(heights) { _, which ->
                val selectedHeight = heights[which]
                binding.tvHeightValue.text = selectedHeight
                saveData("user_height", selectedHeight)
            }
            .show()
    }

    private fun showPickerSheet(title: String, options: Array<String>, key: String, textView: android.widget.TextView) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val picker = view.findViewById<android.widget.NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<android.widget.TextView>(R.id.tv_done)
        val btnCancel = view.findViewById<android.widget.TextView>(R.id.tv_cancel)

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
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val datePicker = view.findViewById<android.widget.DatePicker>(R.id.date_picker)
        val numberPicker = view.findViewById<android.widget.NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<android.widget.TextView>(R.id.tv_done)
        val btnCancel = view.findViewById<android.widget.TextView>(R.id.tv_cancel)

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

    private fun showEditInputSheet(title: String, unit: String, key: String, textView: android.widget.TextView) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val container = view.findViewById<android.widget.LinearLayout>(R.id.number_picker).parent as android.widget.LinearLayout
        view.findViewById<android.widget.NumberPicker>(R.id.number_picker).visibility = View.GONE

        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Enter value"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
        container.addView(input)

        view.findViewById<android.widget.TextView>(R.id.tv_done).setOnClickListener {
            val value = input.text.toString()
            if (value.isNotEmpty()) {
                val result = "$value $unit"
                textView.text = result
                saveData(key, result)
            }
            dialog.dismiss()
        }

        view.findViewById<android.widget.TextView>(R.id.tv_cancel).setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}