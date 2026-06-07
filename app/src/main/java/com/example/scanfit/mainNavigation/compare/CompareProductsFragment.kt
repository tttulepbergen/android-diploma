package com.example.scanfit.mainNavigation.compare

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentCompareProductsBinding
import com.example.scanfit.network.AnalysisRisk
import com.example.scanfit.network.CompareProductsResponse
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.network.NutrientComparison
import com.example.scanfit.utils.SessionManager
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class CompareProductsFragment : Fragment(R.layout.fragment_compare_products) {

    private var _binding: FragmentCompareProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private var imageFileA: File? = null
    private var imageFileB: File? = null
    private var tempCameraFileA: File? = null
    private var tempCameraFileB: File? = null

    private enum class Slot { A, B }

    // ── Activity result launchers ─────────────────────────────────────────────

    private val cameraLauncherA = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempCameraFileA?.let { file ->
                imageFileA = compressFile(file)
                showPreview(Slot.A, imageFileA!!)
                updateCompareButton()
            }
        }
        tempCameraFileA = null
    }

    private val galleryLauncherA = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val file = uriToFile(it, "gallery_a") ?: return@launch
                val compressed = compressFile(file)
                withContext(Dispatchers.Main) {
                    imageFileA = compressed
                    showPreview(Slot.A, compressed)
                    updateCompareButton()
                }
            }
        }
    }

    private val cameraLauncherB = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempCameraFileB?.let { file ->
                imageFileB = compressFile(file)
                showPreview(Slot.B, imageFileB!!)
                updateCompareButton()
            }
        }
        tempCameraFileB = null
    }

    private val galleryLauncherB = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val file = uriToFile(it, "gallery_b") ?: return@launch
                val compressed = compressFile(file)
                withContext(Dispatchers.Main) {
                    imageFileB = compressed
                    showPreview(Slot.B, compressed)
                    updateCompareButton()
                }
            }
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCompareProductsBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Tap on image card → picker dialog
        binding.cardImageA.setOnClickListener { showPickerDialog(Slot.A) }
        binding.cardImageB.setOnClickListener { showPickerDialog(Slot.B) }

        // Individual buttons
        binding.btnCameraA.setOnClickListener { launchCamera(Slot.A) }
        binding.btnGalleryA.setOnClickListener { galleryLauncherA.launch("image/*") }
        binding.btnCameraB.setOnClickListener { launchCamera(Slot.B) }
        binding.btnGalleryB.setOnClickListener { galleryLauncherB.launch("image/*") }

        binding.btnCompare.setOnClickListener { runComparison() }
        updateCompareButton()

        // Pre-fill slot A if navigated from scan screen
        arguments?.getString("captured_image_path")?.let { path ->
            val file = File(path)
            if (file.exists()) {
                imageFileA = file
                showPreview(Slot.A, file)
                updateCompareButton()
            }
        }
    }

    // ── Image picking ─────────────────────────────────────────────────────────

    private fun showPickerDialog(slot: Slot) {
        AlertDialog.Builder(requireContext())
            .setTitle(if (slot == Slot.A) "Add Product A photo" else "Add Product B photo")
            .setItems(arrayOf("Camera", "Gallery")) { _, which ->
                if (which == 0) launchCamera(slot)
                else if (slot == Slot.A) galleryLauncherA.launch("image/*")
                else galleryLauncherB.launch("image/*")
            }
            .show()
    }

    private fun launchCamera(slot: Slot) {
        val file = File(
            requireContext().cacheDir,
            "compare_${slot.name.lowercase(Locale.US)}_${System.currentTimeMillis()}.jpg"
        )
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
        when (slot) {
            Slot.A -> { tempCameraFileA = file; cameraLauncherA.launch(uri) }
            Slot.B -> { tempCameraFileB = file; cameraLauncherB.launch(uri) }
        }
    }

    // ── Image display ─────────────────────────────────────────────────────────

    private fun showPreview(slot: Slot, file: File) {
        val opts = BitmapFactory.Options().apply {
            inSampleSize = 2  // halve resolution to save memory
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return
        when (slot) {
            Slot.A -> {
                binding.ivImageA.setImageBitmap(bitmap)
                binding.layoutPlaceholderA.isVisible = false
            }
            Slot.B -> {
                binding.ivImageB.setImageBitmap(bitmap)
                binding.layoutPlaceholderB.isVisible = false
            }
        }
    }

    private fun updateCompareButton() {
        val ready = imageFileA != null && imageFileB != null
        binding.btnCompare.isEnabled = ready
        binding.btnCompare.backgroundTintList = ColorStateList.valueOf(
            if (ready) Color.parseColor("#1A1C1E") else Color.parseColor("#9CA3AF")
        )
    }

    // ── File helpers ──────────────────────────────────────────────────────────

    private fun uriToFile(uri: Uri, prefix: String): File? {
        return runCatching {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> inputStream.use { it.copyTo(out) } }
            file
        }.getOrElse { e ->
            Log.e("COMPARE", "uriToFile failed: ${e.message}")
            null
        }
    }

    private fun compressFile(file: File): File {
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, opts)
        val compressed = File(requireContext().cacheDir, "cmp_${file.name}")
        FileOutputStream(compressed).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
        }
        bitmap.recycle()
        return compressed
    }

    // ── Comparison ────────────────────────────────────────────────────────────

    private fun runComparison() {
        val fileA = imageFileA ?: return
        val fileB = imageFileB ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            binding.loadingOverlay.isVisible = true
            binding.btnCompare.isEnabled = false

            try {
                val userProfileJson = withContext(Dispatchers.IO) { buildUserProfileJson() }

                val partA = MultipartBody.Part.createFormData(
                    "image_a", fileA.name,
                    fileA.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                val partB = MultipartBody.Part.createFormData(
                    "image_b", fileB.name,
                    fileB.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                val userInfoBody = userProfileJson.toRequestBody("text/plain".toMediaTypeOrNull())

                val result = withContext(Dispatchers.IO) {
                    NetworkClient.aiApiService.compareProductsImages(partA, partB, userInfoBody)
                }
                displayResults(result)

            } catch (e: Exception) {
                Log.e("COMPARE", "Comparison failed: ${e.message}")
                if (isAdded) {
                    Toast.makeText(requireContext(), "Comparison failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (_binding != null) {
                    binding.loadingOverlay.isVisible = false
                    binding.btnCompare.isEnabled = true
                }
            }
        }
    }

    private suspend fun buildUserProfileJson(): String {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) return "{}"
        return runCatching {
            val details = NetworkClient.userApiService.getUserDetails(token).data
            GsonBuilder().serializeNulls().create().toJson(details)
        }.getOrDefault("{}")
    }

    // ── Results display ───────────────────────────────────────────────────────

    private fun displayResults(result: CompareProductsResponse) {
        val nameA = result.nameA?.takeIf { it.isNotBlank() } ?: "Product A"
        val nameB = result.nameB?.takeIf { it.isNotBlank() } ?: "Product B"

        binding.tvVerdictA.text = result.verdictA?.takeIf { it.isNotBlank() } ?: "—"
        binding.tvVerdictB.text = result.verdictB?.takeIf { it.isNotBlank() } ?: "—"

        val winner = result.winner?.uppercase(Locale.US) ?: "TIE"
        val winnerName = result.winnerName?.takeIf { it.isNotBlank() }
            ?: if (winner == "A") nameA else if (winner == "B") nameB else "It's a Tie!"

        val bannerColor = when (winner) {
            "A" -> "#059669"
            "B" -> "#2563EB"
            else -> "#7C3AED"
        }
        binding.cardWinner.setCardBackgroundColor(Color.parseColor(bannerColor))
        binding.tvWinnerLabel.text = if (winner == "TIE") "It's a Tie!" else "Winner"
        binding.tvWinnerName.text = if (winner == "TIE") "Both products are similar" else winnerName

        binding.tvScoreAValue.text = result.healthScoreA?.toString() ?: "—"
        binding.tvScoreBValue.text = result.healthScoreB?.toString() ?: "—"

        binding.tvRecommendation.text = result.recommendation?.takeIf { it.isNotBlank() }
            ?: "No recommendation available."

        binding.nutrientsTable.removeAllViews()
        val nutrients = result.nutrientComparison
        if (!nutrients.isNullOrEmpty()) {
            nutrients.forEach { row -> addNutrientRow(row) }
        }

        val risksA = result.risksA.orEmpty()
        if (risksA.isNotEmpty()) {
            binding.risksAContainer.removeAllViews()
            binding.risksAContainer.addView(buildRisksCard("Risks — $nameA", risksA, "#FFF3E8"))
            binding.risksAContainer.isVisible = true
        }

        val risksB = result.risksB.orEmpty()
        if (risksB.isNotEmpty()) {
            binding.risksBContainer.removeAllViews()
            binding.risksBContainer.addView(buildRisksCard("Risks — $nameB", risksB, "#EFF6FF"))
            binding.risksBContainer.isVisible = true
        }

        binding.resultsContainer.isVisible = true
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.resultsContainer.top)
        }
    }

    private fun addNutrientRow(row: NutrientComparison) {
        val ctx = requireContext()
        val better = row.better?.uppercase(Locale.US)

        val rowLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val dataRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dpToPx(10), 0, dpToPx(6))
        }

        dataRow.addView(TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
            text = row.nutrient ?: ""
            setTextColor(Color.parseColor("#111827"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
        })

        dataRow.addView(TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = row.valueA ?: "—"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            val win = better == "A"
            setTextColor(if (win) Color.parseColor("#059669") else Color.parseColor("#6B7280"))
            if (win) typeface = Typeface.DEFAULT_BOLD
        })

        dataRow.addView(TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = row.valueB ?: "—"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            val win = better == "B"
            setTextColor(if (win) Color.parseColor("#059669") else Color.parseColor("#6B7280"))
            if (win) typeface = Typeface.DEFAULT_BOLD
        })

        dataRow.addView(TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
            text = when (better) { "A" -> "A"; "B" -> "B"; else -> "=" }
            gravity = Gravity.CENTER
            setTextColor(if (better == "A" || better == "B") Color.parseColor("#059669") else Color.parseColor("#D1D5DB"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD
        })

        rowLayout.addView(dataRow)

        row.note?.takeIf { it.isNotBlank() }?.let { note ->
            rowLayout.addView(TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(4) }
                text = note
                setTextColor(Color.parseColor("#9CA3AF"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setLineSpacing(dpToPx(2).toFloat(), 1f)
            })
        }

        rowLayout.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1))
            setBackgroundColor(Color.parseColor("#F3F4F6"))
        })

        binding.nutrientsTable.addView(rowLayout)
    }

    private fun buildRisksCard(
        title: String,
        risks: List<AnalysisRisk>,
        bgColor: String
    ): androidx.cardview.widget.CardView {
        val ctx = requireContext()
        val card = androidx.cardview.widget.CardView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            radius = dpToPx(14).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(Color.parseColor(bgColor))
        }

        val inner = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
        }

        inner.addView(TextView(ctx).apply {
            text = title
            setTextColor(Color.parseColor("#111827"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
        })

        risks.forEach { risk ->
            val severity = risk.severity?.lowercase(Locale.US) ?: "low"
            val severityColor = when (severity) {
                "high" -> "#DC2626"; "medium" -> "#D97706"; else -> "#2563EB"
            }
            val riskRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dpToPx(8), 0, dpToPx(4))
            }
            val header = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            header.addView(TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = risk.ingredient?.takeIf { it.isNotBlank() } ?: "Issue"
                setTextColor(Color.parseColor("#111827"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = Typeface.DEFAULT_BOLD
            })
            header.addView(TextView(ctx).apply {
                text = severity.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                typeface = Typeface.DEFAULT_BOLD
                setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(6).toFloat()
                    setColor(Color.parseColor(severityColor))
                }
            })
            riskRow.addView(header)
            risk.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                riskRow.addView(TextView(ctx).apply {
                    text = reason
                    setTextColor(Color.parseColor("#374151"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setPadding(0, dpToPx(4), 0, 0)
                    setLineSpacing(dpToPx(2).toFloat(), 1f)
                })
            }
            inner.addView(riskRow)
            inner.addView(View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                ).apply { topMargin = dpToPx(4) }
                setBackgroundColor(Color.parseColor("#E5E7EB"))
            })
        }

        card.addView(inner)
        return card
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
