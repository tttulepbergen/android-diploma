package com.example.scanfit.mainNavigation.subscription

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentProSubscriptionBinding
import com.example.scanfit.model.UserAccountData
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.launch

class ProSubscriptionFragment : Fragment(R.layout.fragment_pro_subscription) {

    private var _binding: FragmentProSubscriptionBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var userAccount: UserAccountData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProSubscriptionBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSubscribeNow.setOnClickListener { showContactOptions() }

        renderSubscriptionState()
        loadUserAccount()
    }

    private fun renderSubscriptionState() {
        if (sessionManager.isVip()) {
            binding.btnSubscribeNow.text = "Contact Support"
            binding.tvContactNote.text = "You already have VIP access. Contact us if you need billing help."
        } else {
            binding.btnSubscribeNow.text = "Subscribe Now"
            binding.tvContactNote.text = "Tap Subscribe Now and choose WhatsApp, Telegram, or email to contact us."
        }
    }

    private fun loadUserAccount() {
        val token = sessionManager.fetchAuthToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                NetworkClient.userApiService.getUserAccount(token)
            }.onSuccess { response ->
                if (response.success) {
                    userAccount = response.data
                }
            }
        }
    }

    private fun showContactOptions() {
        val options = arrayOf("WhatsApp", "Telegram", "Email")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose contact method")
            .setItems(options) { _, which ->
                val message = buildSubscriptionMessage()
                when (which) {
                    0 -> openWhatsApp(message)
                    1 -> openTelegram(message)
                    2 -> openEmail(message)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildSubscriptionMessage(): String {
        val email = userAccount?.email ?: "not provided"
        val username = userAccount?.username?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
        return """
            Hello! My name is $username and my ScanFit account email is $email.
            
            I want to buy the ScanFit Pro version for 2990 tenge/month.
            Please help me activate VIP access for my account.
            
            Thank you!
        """.trimIndent()
    }

    private fun openWhatsApp(message: String) {
        val encodedMessage = Uri.encode(message)
        val appIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("whatsapp://send?phone=87007456291&text=$encodedMessage")
        )
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://wa.me/87007456291?text=$encodedMessage")
        )
        openExternalIntent(
            primaryIntent = appIntent,
            fallbackIntent = webIntent,
            errorMessage = "WhatsApp is not available on this device"
        )
    }

    private fun openTelegram(message: String) {
        val encodedMessage = Uri.encode(message)
        val appIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("tg://resolve?domain=aqzsha&text=$encodedMessage")
        )
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://t.me/aqzsha?text=$encodedMessage")
        )
        openExternalIntent(
            primaryIntent = appIntent,
            fallbackIntent = webIntent,
            errorMessage = "Telegram is not available on this device"
        )
    }

    private fun openEmail(message: String) {
        val uri = Uri.parse(
            "mailto:akzholtasbay@gmail.com?subject=${Uri.encode("ScanFit Pro subscription")}&body=${Uri.encode(message)}"
        )
        openExternalIntent(
            primaryIntent = Intent(Intent.ACTION_SENDTO, uri),
            fallbackIntent = null,
            errorMessage = "No email app found on this device"
        )
    }

    private fun openExternalIntent(
        primaryIntent: Intent,
        fallbackIntent: Intent?,
        errorMessage: String
    ) {
        try {
            startActivity(primaryIntent)
        } catch (_: ActivityNotFoundException) {
            if (fallbackIntent != null) {
                try {
                    startActivity(fallbackIntent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
