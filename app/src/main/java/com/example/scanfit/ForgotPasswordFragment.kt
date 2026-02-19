package com.example.scanfit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmailForgot.text.toString().trim()

            if (email.isNotEmpty()) {
                sendResetCode(email)
            } else {
                Toast.makeText(context, "Пожалуйста, введите Email", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun sendResetCode(email: String) {
        val generatedPin = (100000..999999).random().toString()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bundle = Bundle().apply {
                        putString("user_email", email)
                        putString("correct_pin", generatedPin)
                    }


                    Toast.makeText(context, "Код отправлен на вашу почту", Toast.LENGTH_SHORT).show()

                    findNavController().navigate(
                        R.id.action_forgotPasswordFragment_to_verifyPinFragment,
                        bundle
                    )
                } else {
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