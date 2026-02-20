package com.example.scanfit.mainNavigation.user.authorization

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth // Импорт Firebase Auth

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment3_to_forgotPasswordFragment)
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment3_to_signUpFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            val isProfileCompleted = prefs.getBoolean("profile_completed_$uid", false)

                            if (isProfileCompleted) {
                                findNavController().navigate(R.id.action_loginFragment3_to_nav_scan)
                            } else {
                                findNavController().navigate(R.id.action_loginFragment3_to_profileFragment2)
                            }
                        } else {
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