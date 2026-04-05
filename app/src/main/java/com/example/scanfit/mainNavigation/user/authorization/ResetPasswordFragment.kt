package com.example.scanfit.mainNavigation.user.authorization

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentResetPasswordBinding
import com.example.scanfit.model.ResetPasswordRequest
import com.example.scanfit.network.NetworkClient
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResetPasswordBinding.bind(view)

        val email = arguments?.getString("user_email")
        val token = arguments?.getString("reset_token")

        binding.btnResetPassword.setOnClickListener {
            // ViewBinding converts snake_case IDs (et_new_password) to camelCase (etNewPassword)
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmNewPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email != null && token != null) {
                resetPassword(email, newPassword, confirmPassword, token)
            } else {
                Toast.makeText(context, "Error: Missing session data. Try again.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.loginFragment3, false)
            }
        }
    }

    private fun resetPassword(email: String, psw: String, pswConfirm: String, token: String) {
        lifecycleScope.launch {
            try {
                val request = ResetPasswordRequest(
                    email = email,
                    newPassword = psw,
                    newPasswordConfirmation = pswConfirm,
                    token = token
                )
                val response = NetworkClient.authApiService.resetPassword(request)
                
                if (response.success) {
                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment3)
                } else {
                    Toast.makeText(context, response.message ?: "Failed to reset password", Toast.LENGTH_SHORT).show()
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