package com.example.scanfit.mainNavigation.user.authorization

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentLoginBinding
import com.example.scanfit.model.LoginRequest
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        sessionManager = SessionManager(requireContext())

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
                loginUser(email, password)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = NetworkClient.authApiService.login(LoginRequest(email, password))
                if (response.success && response.data != null) {
                    sessionManager.saveAuthToken(response.data.accessToken)
                    sessionManager.saveRefreshToken(response.data.refreshToken)
                    sessionManager.saveUserId(response.data.id)

                    val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val isProfileCompleted = prefs.getBoolean("profile_completed", false)

                    if (isProfileCompleted) {
                        findNavController().navigate(R.id.action_loginFragment3_to_nav_scan)
                    } else {
                        findNavController().navigate(R.id.action_loginFragment3_to_profileFragment2)
                    }
                } else {
                    Toast.makeText(context, response.message ?: "Login failed", Toast.LENGTH_SHORT).show()
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