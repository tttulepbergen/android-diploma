package com.example.scanfit.mainNavigation.user.authorization

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResetPasswordBinding.bind(view)

        binding.btnResetPassword.setOnClickListener {
            // Возвращаемся в самое начало (на логин)
            Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment3)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}