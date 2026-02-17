package com.example.scanfit

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.data.FoodItem
import com.example.scanfit.databinding.FragmentSearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
            android.util.Log.d("SEARCH_CHECK", "Введено: $query")

            searchJob?.cancel()
            if (query.isEmpty()) {
                binding.layoutBarcodePlaceholder.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.layoutBarcodePlaceholder.visibility = View.GONE
                binding.rvSearchResults.visibility = View.VISIBLE

                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    // ПЕРЕДАЕМ В ФОНОВЫЙ ПОТОК (Dispatchers.IO)
                    fetchRealProducts(query)
                }
            }
        }
    }

    private suspend fun fetchRealProducts(query: String) {
        // Указываем, что этот блок кода должен работать в потоке для ввода-вывода (IO)
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("SEARCH_CHECK", "Отправляю запрос в API...")

                // Используем твой NetworkClient
                val response = NetworkClient.apiService.searchProducts(query)

                val foodItems = response.products.map { apiProduct ->
                    FoodItem(
                        title = apiProduct.productName ?: "Unknown",
                        subtitle = apiProduct.brands ?: "No brand",
                        calories = "${apiProduct.nutriments?.energyKcal100g?.toInt() ?: 0} kcal",
                        imageUrl = apiProduct.imageUrl,
                        isFavorite = false
                    )
                }

                // Возвращаемся в Главный поток, чтобы обновить список
                withContext(Dispatchers.Main) {
                    foodAdapter.updateList(foodItems)
                    android.util.Log.d("SEARCH_CHECK", "Успех! Найдено продуктов: ${foodItems.size}")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.util.Log.e("SEARCH_CHECK", "ОШИБКА: ${e.message}")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            items = emptyList(),
            onItemClick = { product -> /* Переход к деталям */ },
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
}