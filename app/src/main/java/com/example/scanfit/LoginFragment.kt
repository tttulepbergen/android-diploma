package com.example.scanfit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scanfit.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth // Импорт Firebase Auth

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        // Переход на восстановление пароля
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment3_to_forgotPasswordFragment)
        }

        // Переход на регистрацию
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment3_to_signUpFragment)
        }

        // Кнопка логина с интеграцией Firebase
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Пытаемся войти через Firebase
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Если вход успешен, переходим в ProfileFragment2 согласно схеме
                            findNavController().navigate(R.id.action_loginFragment3_to_profileFragment2)
                        } else {
                            // Если ошибка (неверный пароль и т.д.), выводим сообщение
                            Toast.makeText(
                                context,
                                "Ошибка: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}