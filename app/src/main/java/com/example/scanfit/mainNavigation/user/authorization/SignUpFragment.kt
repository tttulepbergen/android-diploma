package com.example.scanfit.mainNavigation.user.authorization

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentSignUpBinding
import com.example.scanfit.model.RegisterRequest
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.launch

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        binding.tvSignInLink.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                if (password == confirmPassword) {
                    registerUser(email, name, password, confirmPassword)
                } else {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, name: String, password: String, confirmPassword: String) {
        lifecycleScope.launch {
            try {
                val request = RegisterRequest(
                    email = email,
                    username = name,
                    password = password,
                    passwordConfirmation = confirmPassword
                )
                val response = NetworkClient.authApiService.register(request)
                
                if (response.success) {
                    response.data?.let {
                        sessionManager.saveAuthToken(it.accessToken)
                        sessionManager.saveRefreshToken(it.refreshToken)
                    }
                    
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                    // After registration, usually we go to profile completion or login
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment3)
                } else {
                    Toast.makeText(context, response.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}