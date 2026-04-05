package com.example.scanfit.mainNavigation.user.authorization

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentVerifyPinBinding
import com.example.scanfit.model.VerifyPinRequest
import com.example.scanfit.network.NetworkClient
import kotlinx.coroutines.launch

class VerifyPinFragment : Fragment(R.layout.fragment_verify_pin) {

    private var _binding: FragmentVerifyPinBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerifyPinBinding.bind(view)

        val userEmail = arguments?.getString("user_email")
        binding.tvEmailDisplay.text = userEmail ?: "your email"

        setupPinInputs()

        binding.btnVerify.setOnClickListener {
            val enteredPin = collectPinFromInputs()

            if (enteredPin.length < 6) {
                Toast.makeText(context, "Введите все 6 цифр", Toast.LENGTH_SHORT).show()
            } else if (userEmail != null) {
                verifyPin(userEmail, enteredPin)
            }
        }

        binding.tvResendCode.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun verifyPin(email: String, pin: String) {
        lifecycleScope.launch {
            try {
                val response = NetworkClient.authApiService.verifyPin(VerifyPinRequest(email, pin))
                if (response.success && response.data != null) {
                    val bundle = Bundle().apply {
                        putString("user_email", email)
                        putString("reset_token", response.data.token)
                    }
                    Toast.makeText(context, "Код подтвержден!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_verifyPinFragment_to_resetPasswordFragment, bundle)
                } else {
                    Toast.makeText(context, response.message ?: "Неверный код", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collectPinFromInputs(): String {
        return binding.etDigit1.text.toString() +
                binding.etDigit2.text.toString() +
                binding.etDigit3.text.toString() +
                binding.etDigit4.text.toString() +
                binding.etDigit5.text.toString() +
                binding.etDigit6.text.toString()
    }

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
                        editTexts[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}