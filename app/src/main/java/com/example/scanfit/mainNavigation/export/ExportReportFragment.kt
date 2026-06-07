package com.example.scanfit.mainNavigation.export

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentExportReportBinding
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExportReportFragment : Fragment(R.layout.fragment_export_report) {

    private var _binding: FragmentExportReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    private var fromCal: Calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -29) }
    private var toCal: Calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExportReportBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        updateDateLabels()
        binding.layoutDateFrom.setOnClickListener { showDatePicker(isFrom = true) }
        binding.layoutDateTo.setOnClickListener { showDatePicker(isFrom = false) }
        binding.btnGenerate.setOnClickListener { generateReport() }
    }

    private fun updateDateLabels() {
        binding.tvDateFrom.text = displayFormat.format(fromCal.time)
        binding.tvDateTo.text = displayFormat.format(toCal.time)
    }

    private fun showDatePicker(isFrom: Boolean) {
        val cal = if (isFrom) fromCal else toCal
        android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                if (isFrom) {
                    fromCal = Calendar.getInstance().apply { set(year, month, day) }
                } else {
                    toCal = Calendar.getInstance().apply { set(year, month, day) }
                }
                updateDateLabels()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun generateReport() {
        if (fromCal.after(toCal)) {
            Toast.makeText(requireContext(), "Start date must be before end date", Toast.LENGTH_SHORT).show()
            return
        }
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
            return
        }
        val isPdf = binding.radioGroupFormat.checkedRadioButtonId == R.id.rb_pdf
        val from = dateFormat.format(fromCal.time)
        val to = dateFormat.format(toCal.time)

        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    if (isPdf) downloadPdf(token, from, to)
                    else generateCsv(token, from, to)
                }
                if (isAdded && _binding != null) {
                    setLoading(false)
                    shareFile(file, isPdf)
                }
            } catch (e: Exception) {
                Log.e("EXPORT", "Report generation failed", e)
                if (isAdded) {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun downloadPdf(token: String, from: String, to: String): File {
        val body = NetworkClient.userApiService.downloadConsumptionReportPdf(token, from, to)
        val file = File(requireContext().cacheDir, "ScanFit_Report_${from}_${to}.pdf")
        FileOutputStream(file).use { out ->
            body.byteStream().use { it.copyTo(out) }
        }
        return file
    }

    private suspend fun generateCsv(token: String, from: String, to: String): File {
        val resp = NetworkClient.userApiService.getConsumptionHistory(token, from, to)
        if (!resp.success) throw Exception(resp.message ?: "Failed to load data")
        val items = resp.data.orEmpty()

        val sb = StringBuilder()
        sb.appendLine("ScanFit Health Report")
        sb.appendLine("Period,$from,$to")
        sb.appendLine("Generated,${dateFormat.format(java.util.Date())}")
        sb.appendLine()
        sb.appendLine("Date,Calories,Cal Goal,Protein (g),Protein Goal,Carbs (g),Carbs Goal,Fat (g),Fat Goal,Water (ml),Water Goal,Sodium (mg),Fiber (g),Sugar (g),Cholesterol (mg),Vit A (mcg),Vit B6 (mg),Vit B9 (mcg),Vit B12 (mcg),Vit C (mg),Vit D (mcg),Vit E (mg)")
        for (item in items.sortedByDescending { it.date.orEmpty() }) {
            sb.appendLine(
                "${item.date ?: ""},${item.calories ?: 0},${item.caloriesGoal ?: 0}," +
                "${item.proteins ?: 0},${item.proteinGoal ?: 0}," +
                "${item.carbs ?: 0},${item.carbsGoal ?: 0}," +
                "${item.fat ?: 0},${item.fatGoal ?: 0}," +
                "${item.water ?: 0},${item.waterGoal ?: 0}," +
                "${item.sodium ?: 0},${item.fiber ?: 0},${item.sugar ?: 0},${item.cholesterol ?: 0}," +
                "${fmt(item.vitaminA)},${fmt(item.vitaminB6)},${fmt(item.vitaminB9)},${fmt(item.vitaminB12)}," +
                "${fmt(item.vitaminC)},${fmt(item.vitaminD)},${fmt(item.vitaminE)}"
            )
        }
        if (items.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("--- AVERAGES ---")
            sb.appendLine("Avg Calories,${items.map { it.calories ?: 0 }.average().toInt()}")
            sb.appendLine("Avg Protein (g),${items.map { it.proteins ?: 0 }.average().toInt()}")
            sb.appendLine("Avg Carbs (g),${items.map { it.carbs ?: 0 }.average().toInt()}")
            sb.appendLine("Avg Fat (g),${items.map { it.fat ?: 0 }.average().toInt()}")
            sb.appendLine("Avg Water (L),${String.format(Locale.US, "%.2f", items.map { it.water ?: 0 }.average() / 1000.0)}")
        }

        val file = File(requireContext().cacheDir, "ScanFit_Report_${from}_${to}.csv")
        file.writeText(sb.toString())
        return file
    }

    private fun fmt(v: Double?): String = String.format(Locale.US, "%.2f", v ?: 0.0)

    private fun shareFile(file: File, isPdf: Boolean) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (isPdf) "application/pdf" else "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ScanFit Health Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Report"))
    }

    private fun setLoading(loading: Boolean) {
        if (_binding == null) return
        binding.loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnGenerate.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
