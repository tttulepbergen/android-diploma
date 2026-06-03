package com.example.scanfit.mainNavigation

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentConsumptionHistoryBinding
import com.example.scanfit.model.ConsumptionHistoryItem
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class ConsumptionHistoryFragment : Fragment(R.layout.fragment_consumption_history) {

    private var _binding: FragmentConsumptionHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConsumptionHistoryBinding.bind(view)
        sessionManager = SessionManager(requireContext())

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        loadHistory()
    }

    private fun loadHistory() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDate = arguments?.getString(ARG_SELECTED_DATE)
        val toDate = selectedDate ?: API_DATE_FORMAT.format(Calendar.getInstance().time)
        val fromDate = buildFromDate(toDate)

        binding.tvHistoryTitle.text = "History"
        binding.tvHistorySubtitle.text = "$fromDate to $toDate"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = NetworkClient.userApiService.getConsumptionHistory(token, fromDate, toDate)
                if (response.success) {
                    renderHistory(response.data.orEmpty())
                } else {
                    showEmptyState()
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "Failed to load history",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                showEmptyState()
                Toast.makeText(
                    requireContext(),
                    e.message ?: "Failed to load history",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun buildFromDate(toDate: String): String {
        val calendar = Calendar.getInstance().apply {
            time = API_DATE_FORMAT.parse(toDate) ?: Calendar.getInstance().time
            add(Calendar.DAY_OF_YEAR, -6)
        }
        return API_DATE_FORMAT.format(calendar.time)
    }

    private fun renderHistory(items: List<ConsumptionHistoryItem>) {
        binding.historyListContainer.removeAllViews()
        binding.chartContainer.removeAllViews()
        binding.tvEmptyHistory.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

        if (items.isEmpty()) {
            binding.tvAvgCalories.text = "0 kcal"
            binding.tvAvgWater.text = "0.00 L"
            return
        }

        val avgCalories = items.map { it.calories ?: 0 }.average().roundToInt()
        val avgWaterLiters = items.map { it.water ?: 0 }.average() / 1000.0
        binding.tvAvgCalories.text = "$avgCalories kcal"
        binding.tvAvgWater.text = String.format(Locale.US, "%.2f L", avgWaterLiters)

        renderChart(items)
        items.sortedByDescending { it.date.orEmpty() }.forEach { item ->
            binding.historyListContainer.addView(createHistoryCard(item))
        }
    }

    private fun renderChart(items: List<ConsumptionHistoryItem>) {
        val maxCalories = (items.maxOfOrNull { it.calories ?: 0 } ?: 0).coerceAtLeast(1)
        items.forEach { item ->
            val column = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                    marginEnd = dpToPx(6)
                }
            }

            val barHeight = ((item.calories ?: 0).toFloat() / maxCalories * dpToPx(120)).roundToInt().coerceAtLeast(dpToPx(8))
            val bar = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(24), barHeight)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(12).toFloat()
                    setColor(Color.parseColor("#17A34A"))
                }
            }

            val caloriesLabel = TextView(requireContext()).apply {
                text = "${item.calories ?: 0}"
                setTextColor(Color.parseColor("#111827"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dpToPx(6))
            }

            val dateLabel = TextView(requireContext()).apply {
                text = item.date?.takeLast(2) ?: "--"
                setTextColor(Color.parseColor("#6B7280"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(8), 0, 0)
            }

            column.addView(caloriesLabel)
            column.addView(bar)
            column.addView(dateLabel)
            binding.chartContainer.addView(column)
        }
    }

    private fun createHistoryCard(item: ConsumptionHistoryItem): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(22).toFloat()
                setColor(Color.WHITE)
                setStroke(dpToPx(1), Color.parseColor("#E5E7EB"))
            }
            setPadding(dpToPx(18), dpToPx(18), dpToPx(18), dpToPx(18))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }

            addView(TextView(requireContext()).apply {
                text = item.date ?: "Unknown date"
                setTextColor(Color.parseColor("#111827"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                typeface = Typeface.DEFAULT_BOLD
            })

            addView(createMetricRow("Calories", "${item.calories ?: 0} / ${item.caloriesGoal ?: 0} kcal"))
            addView(createMetricRow("Carbs", "${item.carbs ?: 0} / ${item.carbsGoal ?: 0} g"))
            addView(createMetricRow("Protein", "${item.proteins ?: 0} / ${item.proteinGoal ?: 0} g"))
            addView(createMetricRow("Fats", "${item.fat ?: 0} / ${item.fatGoal ?: 0} g"))
            addView(createMetricRow("Water", "${formatLiters(item.water ?: 0)} / ${formatLiters(item.waterGoal ?: 0)}"))
            addView(createMetricRow("Sodium", "${item.sodium ?: 0} / ${item.sodiumGoal ?: 0} mg", isPremium = true))
            addView(createMetricRow("Fiber", "${item.fiber ?: 0} / ${item.fiberGoal ?: 0} g", isPremium = true))
            addView(createMetricRow("Sugar", "${item.sugar ?: 0} / ${item.sugarGoal ?: 0} g", isPremium = true))
            addView(createMetricRow("Cholesterol", "${item.cholesterol ?: 0} / ${item.cholesterolGoal ?: 0} mg", isPremium = true))
            addView(createMetricRow("Vitamin A", "${formatNumber(item.vitaminA)} / ${formatNumber(item.vitaminAGoal)} mcg", isPremium = true))
            addView(createMetricRow("Vitamin B12", "${formatNumber(item.vitaminB12)} / ${formatNumber(item.vitaminB12Goal)} mcg", isPremium = true))
            addView(createMetricRow("Vitamin B6", "${formatNumber(item.vitaminB6)} / ${formatNumber(item.vitaminB6Goal)} mg", isPremium = true))
            addView(createMetricRow("Vitamin B9", "${formatNumber(item.vitaminB9)} / ${formatNumber(item.vitaminB9Goal)} mcg", isPremium = true))
            addView(createMetricRow("Vitamin C", "${formatNumber(item.vitaminC)} / ${formatNumber(item.vitaminCGoal)} mg", isPremium = true))
            addView(createMetricRow("Vitamin D", "${formatNumber(item.vitaminD)} / ${formatNumber(item.vitaminDGoal)} mcg", isPremium = true))
            addView(createMetricRow("Vitamin E", "${formatNumber(item.vitaminE)} / ${formatNumber(item.vitaminEGoal)} mg", isPremium = true))
        }
    }

    private fun createMetricRow(label: String, value: String, isPremium: Boolean = false): View {
        val isLocked = isPremium && !sessionManager.isVip()
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dpToPx(10), 0, 0)

            addView(TextView(requireContext()).apply {
                text = label
                setTextColor(Color.parseColor(if (isLocked) "#9CA3AF" else "#374151"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(requireContext()).apply {
                text = if (isLocked) "ScanFit Pro" else value
                setTextColor(Color.parseColor(if (isLocked) "#2F6BFF" else "#111827"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isLocked) 13f else 14f)
                typeface = Typeface.DEFAULT_BOLD
                if (isLocked) {
                    setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = dpToPx(50).toFloat()
                        setColor(Color.parseColor("#E8F0FF"))
                    }
                }
            })
        }
    }

    private fun showEmptyState() {
        renderHistory(emptyList())
    }

    private fun formatLiters(amountMl: Int): String {
        return String.format(Locale.US, "%.2f L", amountMl / 1000f)
    }

    private fun formatNumber(value: Double?): String {
        val safe = value ?: 0.0
        return if (safe % 1.0 == 0.0) {
            safe.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", safe).trimEnd('0').trimEnd('.')
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SELECTED_DATE = "selected_date"
        private val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}
