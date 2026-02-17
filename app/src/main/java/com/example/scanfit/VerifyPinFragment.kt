package com.example.scanfit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scanfit.databinding.FragmentVerifyPinBinding

class VerifyPinFragment : Fragment(R.layout.fragment_verify_pin) {

    private var _binding: FragmentVerifyPinBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerifyPinBinding.bind(view)

        // 1. Получаем правильный PIN и Email из Bundle
        val correctPin = arguments?.getString("correct_pin")
        val userEmail = arguments?.getString("user_email")

        // Отображаем email пользователя на экране (убедись, что ID совпадает)
        binding.tvEmailDisplay.text = userEmail ?: "your email"

        // 2. Настраиваем автоматический переход между полями ввода
        setupPinInputs()

        // 3. Обработка кнопки подтверждения
        binding.btnVerify.setOnClickListener {
            val enteredPin = collectPinFromInputs()

            if (enteredPin.length < 6) {
                Toast.makeText(context, "Введите все 6 цифр", Toast.LENGTH_SHORT).show()
            } else if (enteredPin == correctPin) {
                // УСПЕХ: Код совпал!
                Toast.makeText(context, "Код подтвержден!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_verifyPinFragment_to_resetPasswordFragment)
            } else {
                // ОШИБКА: Код неверный
                Toast.makeText(context, "Неверный код. Попробуйте еще раз", Toast.LENGTH_SHORT).show()
                clearInputs() // Очистить поля для повторного ввода
            }
        }

        // Кнопка "Resend" (назад на экран ввода почты)
        binding.tvResendCode.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Собираем все цифры из 6 полей в одну строку
    private fun collectPinFromInputs(): String {
        return binding.etDigit1.text.toString() +
                binding.etDigit2.text.toString() +
                binding.etDigit3.text.toString() +
                binding.etDigit4.text.toString() +
                binding.etDigit5.text.toString() +
                binding.etDigit6.text.toString()
    }

    private fun clearInputs() {
        binding.etDigit1.text.clear()
        binding.etDigit2.text.clear()
        binding.etDigit3.text.clear()
        binding.etDigit4.text.clear()
        binding.etDigit5.text.clear()
        binding.etDigit6.text.clear()
        binding.etDigit1.requestFocus() // Возвращаем фокус на первое поле
    }

    // Логика автоматического переключения фокуса
    private fun setupPinInputs() {
        val editTexts = arrayOf(
            binding.etDigit1, binding.etDigit2, binding.etDigit3,
            binding.etDigit4, binding.etDigit5, binding.etDigit6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < editTexts.size - 1) {
                        editTexts[i + 1].requestFocus() // Прыжок вперед
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    if (s?.isEmpty() == true && i > 0) {
                        editTexts[i - 1].requestFocus() // Прыжок назад при удалении
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}