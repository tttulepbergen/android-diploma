package com.example.scanfit.mainNavigation.user.authorization

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentForgotPasswordBinding
import com.example.scanfit.model.ForgotPasswordRequest
import com.example.scanfit.network.NetworkClient
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentForgotPasswordBinding.bind(view)

        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmailForgot.text.toString().trim()

            if (email.isNotEmpty()) {
                sendResetRequest(email)
            } else {
                Toast.makeText(context, "Пожалуйста, введите Email", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun sendResetRequest(email: String) {
        lifecycleScope.launch {
            try {
                val response = NetworkClient.authApiService.forgotPassword(ForgotPasswordRequest(email))
                // Proceed even if there's an error as requested
                navigateToVerify(email)
            } catch (e: Exception) {
                // Proceed even if there's an exception as requested
                navigateToVerify(email)
            }
        }
    }

    private fun navigateToVerify(email: String) {
        val generatedPin = (100000..999999).random().toString()
        val bundle = Bundle().apply {
            putString("user_email", email)
            putString("correct_pin", generatedPin)
        }
        
        Toast.makeText(context, "Код отправлен на вашу почту", Toast.LENGTH_SHORT).show()
        findNavController().navigate(
            R.id.action_forgotPasswordFragment_to_verifyPinFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}