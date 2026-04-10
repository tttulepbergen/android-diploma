package com.example.scanfit.mainNavigation.browse.products

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.R
import com.example.scanfit.adapters.FoodAdapter
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.data.FavoriteProduct
import com.example.scanfit.data.FoodItem
import com.example.scanfit.data.RecentProduct // Твой импорт
import com.example.scanfit.databinding.FragmentProductListBinding
import com.example.scanfit.network.NetworkClient // Пакет от Sunbekova
import kotlinx.coroutines.launch

class ProductListFragment : Fragment(R.layout.fragment_product_list) {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val apiService = NetworkClient.apiService
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var foodAdapter: FoodAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductListBinding.bind(view)

        val subCategory = arguments?.getString("subCategoryName") ?: "Products"
        binding.tvTitle.text = subCategory

        setupRecyclerView()
        loadProducts(subCategory)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            items = emptyList(),
            onItemClick = { selectedProduct ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val recentEntity = RecentProduct(
                        id = selectedProduct.title,
                        title = selectedProduct.title,
                        subtitle = selectedProduct.subtitle,
                        imageUrl = selectedProduct.imageUrl,
                        calories = selectedProduct.calories,
                        grade = selectedProduct.grade,
                        timestamp = System.currentTimeMillis(),
                        ingredients = selectedProduct.ingredients
                    )
                    database.productDao().insertRecent(recentEntity)
                }

                val bundle = bundleOf("foodItem" to selectedProduct)
                findNavController().navigate(
                    R.id.action_productListFragment_to_productDetailFragment,
                    bundle
                )
            },
            onFavoriteClick = { clickedItem ->
                handleFavoriteAction(clickedItem)
            },
            showFavoriteIcon = true
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
        }
    }

    private fun loadProducts(category: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiCategory = "en:${category.lowercase().replace(" ", "-")}"
                val response = apiService.getProductsByCategory(category = apiCategory)
                val favoritesIds = database.productDao().getAllFavoritesOnce().map { it.id }.toSet()

                val products = response.products.map { product ->
                    val id = product.productName ?: "Unknown"
                    val isFav = favoritesIds.contains(id)
                    val nut = product.nutriments
                    val estimated = product.nutrimentsEstimated

                    val energy = nut?.energyKcal100g ?: nut?.energyKcalServing ?: product.energyKcal100g ?: 0.0

                    val ingredientsSource = product.ingredientsText ?: product.ingredientsTextEn

                    FoodItem(
                        title = product.productName ?: "Unknown Product",
                        subtitle = product.brands ?: "No brand",
                        imageUrl = product.imageUrl ?: "",
                        calories = "${energy.toInt()} cal",
                        grade = product.nutriscoreGrade?.uppercase() ?: "B",
                        isFavorite = isFav,
                        proteins = "${nut?.proteins100g ?: nut?.proteinsServing ?: 0.0}g",
                        fat = "${nut?.fat100g ?: nut?.fatServing ?: 0.0}g",
                        carbs = "${nut?.carbohydrates100g ?: nut?.carbohydratesServing ?: 0.0}g",
                        sugars = "${nut?.sugars100g ?: nut?.sugarsServing ?: 0.0}g",
                        fiber = "${nut?.fiber100g ?: nut?.fiberServing ?: 0.0}g",
                        sodium = "${((nut?.sodium100g ?: nut?.sodiumServing ?: 0.0) * 1000).toInt()}mg",
                        cholesterol = "${nut?.cholesterol100g ?: nut?.cholesterolServing ?: 0.0}mg",
                        vitaminD = formatMicrograms(nut?.vitaminDServing ?: nut?.vitaminD100g ?: estimated?.vitaminDServing ?: estimated?.vitaminD100g),
                        vitaminB12 = formatMicrograms(nut?.vitaminB12Serving ?: nut?.vitaminB12100g ?: estimated?.vitaminB12Serving ?: estimated?.vitaminB12100g),
                        vitaminC = formatMilligrams(nut?.vitaminCServing ?: nut?.vitaminC100g ?: estimated?.vitaminCServing ?: estimated?.vitaminC100g),
                        vitaminA = formatMicrograms(nut?.vitaminAServing ?: nut?.vitaminA100g ?: estimated?.vitaminAServing ?: estimated?.vitaminA100g),
                        vitaminB6 = formatMilligrams(nut?.vitaminB6Serving ?: nut?.vitaminB6100g ?: estimated?.vitaminB6Serving ?: estimated?.vitaminB6100g),
                        vitaminB9 = formatMicrograms(nut?.vitaminB9Serving ?: nut?.vitaminB9100g ?: nut?.folatesServing ?: nut?.folates100g ?: estimated?.vitaminB9Serving ?: estimated?.vitaminB9100g ?: estimated?.folatesServing ?: estimated?.folates100g),
                        vitaminE = formatMilligrams(nut?.vitaminEServing ?: nut?.vitaminE100g ?: estimated?.vitaminEServing ?: estimated?.vitaminE100g),
                        ingredients = if (ingredientsSource.isNullOrBlank()) "" else ingredientsSource
                    )
                }
                foodAdapter.updateList(products)
            } catch (e: Exception) {
                Toast.makeText(context, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleFavoriteAction(item: FoodItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            val id = item.title
            if (item.isFavorite) {
                val entity = FavoriteProduct(
                    id = id,
                    productName = item.title,
                    name = item.title,
                    imageUrl = item.imageUrl,
                    calories = item.calories,
                    grade = item.grade,
                    ingredients = item.ingredients
                )
                database.productDao().insertFavorite(entity)
                Toast.makeText(context, "Saved to favourites", Toast.LENGTH_SHORT).show()
            } else {
                database.productDao().deleteFavoriteById(id)
                Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show()
            }
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
