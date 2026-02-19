package com.example.scanfit.mainNavigation.user.authorization

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentForgotPasswordBinding.bind(view)

        auth = FirebaseAuth.getInstance()

        // Кнопка "Send reset link" (убедись, что ID в XML совпадает)
        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmailForgot.text.toString().trim()

            if (email.isNotEmpty()) {
                sendResetCode(email)
            } else {
                Toast.makeText(context, "Пожалуйста, введите Email", Toast.LENGTH_SHORT).show()
            }
        }

        // Ссылка "Back to Login"
        binding.tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun sendResetCode(email: String) {
        // 1. Генерируем случайный 6-значный PIN-код
        val generatedPin = (100000..999999).random().toString()

        // 2. Отправляем запрос в Firebase
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Письмо ушло! Теперь передаем сгенерированный PIN на следующий экран
                    val bundle = Bundle().apply {
                        putString("user_email", email)
                        putString("correct_pin", generatedPin)
                    }


                    Toast.makeText(context, "Код отправлен на вашу почту", Toast.LENGTH_SHORT).show()

                    // Переходим на экран Verify PIN
                    findNavController().navigate(
                        R.id.action_forgotPasswordFragment_to_verifyPinFragment,
                        bundle
                    )
                } else {
                    // Если почта не найдена в базе или нет интернета
                    Toast.makeText(
                        context,
                        "Ошибка: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}