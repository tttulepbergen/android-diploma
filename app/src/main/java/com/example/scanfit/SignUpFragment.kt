package com.example.scanfit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth // Импортируем Firebase
import com.example.scanfit.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    // 1. Объявляем переменную для работы с Firebase
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpBinding.bind(view)

        // 2. Инициализируем Firebase
        auth = FirebaseAuth.getInstance()

        binding.tvSignInLink.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegister.setOnClickListener {
            // 3. Получаем данные из полей ввода
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()

            // Простая проверка на заполнение
            if (email.isNotEmpty() && password.length >= 6) {

                // 4. САМАЯ ВАЖНАЯ ЧАСТЬ: Отправка данных в Firebase
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Успешно! Пользователь появится в консоли Firebase
                            Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment3)
                        } else {
                            // Ошибка (например, email уже занят или нет интернета)
                            Toast.makeText(context, "Ошибка: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Пароль должен быть больше 6 символов", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}