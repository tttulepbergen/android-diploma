package com.example.scanfit.mainNavigation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.adapters.FoodAdapter
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.R
import com.example.scanfit.data.FoodItem
import com.example.scanfit.databinding.FragmentSearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.os.bundleOf

// ... (импорты остаются те же)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodAdapter: FoodAdapter
    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        setupRecyclerView()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            Log.d("SEARCH_CHECK", "Введено: $query")

            searchJob?.cancel()
            if (query.isEmpty()) {
                binding.layoutBarcodePlaceholder.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.layoutBarcodePlaceholder.visibility = View.GONE
                binding.rvSearchResults.visibility = View.VISIBLE

                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    fetchRealProducts(query)
                }
            }
        }
    }

    private suspend fun fetchRealProducts(query: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("SEARCH_CHECK", "Отправляю запрос в API...")

                val response = NetworkClient.apiService.searchProducts(query)

                val foodItems = response.products.map { apiProduct ->
                    val nut = apiProduct.nutriments
                    val estimated = apiProduct.nutrimentsEstimated
                    FoodItem(
                        title = apiProduct.productName ?: "Unknown",
                        subtitle = apiProduct.brands ?: "No brand",
                        calories = "${nut?.energyKcal100g?.toInt() ?: 0} kcal",
                        imageUrl = apiProduct.imageUrl,
                        isFavorite = false,
                        vitaminD = formatMicrograms(nut?.vitaminDServing ?: nut?.vitaminD100g ?: estimated?.vitaminDServing ?: estimated?.vitaminD100g),
                        vitaminB12 = formatMicrograms(nut?.vitaminB12Serving ?: nut?.vitaminB12100g ?: estimated?.vitaminB12Serving ?: estimated?.vitaminB12100g),
                        vitaminC = formatMilligrams(nut?.vitaminCServing ?: nut?.vitaminC100g ?: estimated?.vitaminCServing ?: estimated?.vitaminC100g),
                        vitaminA = formatMicrograms(nut?.vitaminAServing ?: nut?.vitaminA100g ?: estimated?.vitaminAServing ?: estimated?.vitaminA100g),
                        vitaminB6 = formatMilligrams(nut?.vitaminB6Serving ?: nut?.vitaminB6100g ?: estimated?.vitaminB6Serving ?: estimated?.vitaminB6100g),
                        vitaminB9 = formatMicrograms(nut?.vitaminB9Serving ?: nut?.vitaminB9100g ?: nut?.folatesServing ?: nut?.folates100g ?: estimated?.vitaminB9Serving ?: estimated?.vitaminB9100g ?: estimated?.folatesServing ?: estimated?.folates100g),
                        vitaminE = formatMilligrams(nut?.vitaminEServing ?: nut?.vitaminE100g ?: estimated?.vitaminEServing ?: estimated?.vitaminE100g),
                        ingredients = apiProduct.ingredientsText ?: apiProduct.ingredientsTextEn ?: ""
                    )
                }

                withContext(Dispatchers.Main) {
                    foodAdapter.updateList(foodItems)
                    Log.d("SEARCH_CHECK", "Успех! Найдено продуктов: ${foodItems.size}")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SEARCH_CHECK", "ОШИБКА: ${e.message}")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            items = emptyList(),
            onItemClick = { product ->
                val bundle = bundleOf("foodItem" to product)
                findNavController().navigate(
                    R.id.action_searchFragment_to_productDetailFragment,
                    bundle
                ) },
            showFavoriteIcon = true,
            showDetails = true
        )
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatMilligrams(valueInGrams: Double?): String {
        return "${((valueInGrams ?: 0.0) * 1000).formatAmount()} mg"
    }

    private fun formatMicrograms(valueInGrams: Double?): String {
        return "${((valueInGrams ?: 0.0) * 1_000_000).formatAmount()} mcg"
    }

    private fun Double.formatAmount(): String {
        return if (this % 1.0 == 0.0) {
            this.toInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
        }
    }
}
