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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        // 1. Получаем данные пользователя из Firebase
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            // Устанавливаем почту
            binding.tvProfileEmail.text = it.email
            // Устанавливаем имя (если пусто, берем часть почты до @)
            binding.tvProfileUsername.text = it.displayName ?: it.email?.substringBefore("@") ?: "User"
        }

        // 2. Логика кнопки "Назад"
        binding.btnBack.setOnClickListener {
            // Возвращаемся на предыдущий экран в NavGraph
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
        // Показываем блок аккаунта, скрываем измерения
        binding.layoutAccountInfo.visibility = View.VISIBLE
        binding.layoutMeasurements.visibility = View.GONE

        // Возвращаем кнопки логаута (если они нужны только тут)
        binding.btnLogout.visibility = View.VISIBLE
        binding.btnDeleteAccount.visibility = View.VISIBLE
    }

    private fun showMeasurements() {
        // Скрываем блок аккаунта, показываем измерения
        binding.layoutAccountInfo.visibility = View.GONE
        binding.layoutMeasurements.visibility = View.VISIBLE

        // Скрываем кнопки логаута, чтобы экран был похож на твой референс
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE

        // Вызываем загрузку данных и настройку кликов
        loadUserMeasurements()
        setupMeasurementClickListeners()
    }

    private fun setupMeasurementClickListeners() {
        // 1. Пол (3 опции + ползунок)
        binding.rowGender.setOnClickListener {
            showPickerSheet("I am a", arrayOf("Gal", "Guy", "Prefer not to say"), "user_gender", binding.tvGenderValue)
        }

        // 2. Дата рождения (как на фото)
        binding.rowBirth.setOnClickListener {
            showDatePickerSheet()
        }

        // 3. Рост (Клавиатура)
        binding.rowHeight.setOnClickListener {
            showEditInputSheet("My height is", "cm", "user_height", binding.tvHeightValue)
        }

        // 4. Вес (Клавиатура)
        binding.rowWeight.setOnClickListener {
            showEditInputSheet("My current weight is", "kg", "user_weight", binding.tvWeightValue)
        }
    }

    private fun loadUserMeasurements() {
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Подтягиваем данные, сохраненные в ProfileFragment при регистрации
        binding.tvHeightValue.text = prefs.getString("user_height", "not set")
        binding.tvWeightValue.text = prefs.getString("user_weight", "not set")
        binding.tvGenderValue.text = prefs.getString("user_gender", "please select")
    }

    private fun saveData(key: String, value: String) {
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }


    private fun setupButtons() {
        // Кнопка выхода из аккаунта
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()

            // Если у тебя в nav_graph есть переход на логин, укажи его ID
            // findNavController().navigate(R.id.action_userProfileFragment_to_loginFragment)

            // Либо просто закрыть активити, если логин — это другая активити
            requireActivity().finish()
        }

        // Кнопка удаления аккаунта (для диплома это хороший плюс)
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
                binding.tvGenderValue.text = selected // ДОБАВЬ ЭТУ СТРОКУ
                saveData("user_gender", selected)
            }
            .show()
    }

    private fun showWeightInputDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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
        // Создаем список роста от 140 до 220 см
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

    private fun showPickerSheet(title: String, options: Array<String>, key: String, textView: TextView) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        val picker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.tv_done)
        val btnCancel = view.findViewById<TextView>(R.id.tv_cancel)

        // Настройка ползунка
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
            binding.tvBirthValue.text = dateString // Убедись, что такой ID есть в XML
            saveData("user_birth", dateString)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showEditInputSheet(title: String, unit: String, key: String, textView: TextView) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_picker_bottom_sheet, null)

        // Вместо пикера программно добавим EditText для ввода
        val container = view.findViewById<LinearLayout>(R.id.number_picker).parent as LinearLayout
        view.findViewById<NumberPicker>(R.id.number_picker).visibility = View.GONE

        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Enter value"
            textSize = 24f // Изменили sp на f
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