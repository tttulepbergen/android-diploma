package com.example.scanfit.mainNavigation.browse.categories

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.adapters.FoodAdapter
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentCategoriesBinding
import com.example.scanfit.data.FoodItem

class CategoriesFragment : Fragment(R.layout.fragment_categories) {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodAdapter: FoodAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoriesBinding.bind(view)
        setupRecyclerView()
        loadCategories()
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            items = emptyList(),
            onItemClick = { selectedItem ->
                val bundle = bundleOf("categoryName" to selectedItem.title)
                findNavController().navigate(
                    R.id.action_categoriesFragment_to_subCategoriesFragment,
                    bundle
                )
            },
            onFavoriteClick = { /* логика */ },
            showDetails = false // СКРЫВАЕМ
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
        }
    }

    private fun updateUI(newList: List<FoodItem>) {
        foodAdapter = FoodAdapter(
            items = newList,
            onItemClick = { selectedItem ->
                val bundle = bundleOf("categoryName" to selectedItem.title)
                findNavController().navigate(
                    R.id.action_categoriesFragment_to_subCategoriesFragment,
                    bundle
                )
            },
            onFavoriteClick = { },
            showDetails = false // СКРЫВАЕМ
        )
        binding.recyclerView.adapter = foodAdapter
    }

    private fun loadCategories() {
        // ИСПОЛЬЗУЕМ ИМЕНОВАННЫЕ АРГУМЕНТЫ, чтобы исправить ошибки Type Mismatch
        val localList = listOf(
            FoodItem(title = "Fruits & Vegetables", subtitle = "Fresh picks", imageRes = R.drawable.ic_fruits),
            FoodItem(title = "Breads & Carbs", subtitle = "Bakery", imageRes = R.drawable.ic_bread),
            FoodItem(title = "Breakfast", subtitle = "Morning energy", imageRes = R.drawable.ic_breakfast),
            FoodItem(title = "Dairy & Eggs", subtitle = "Farm products", imageRes = R.drawable.ic_eggs),
            FoodItem(title = "Meat & Fish", subtitle = "Proteins", imageRes = R.drawable.ic_meat),
            FoodItem(title = "Other Proteins", subtitle = "Alternative proteins", imageRes = R.drawable.ic_proteins),
            FoodItem(title = "Condiments & More", subtitle = "Sauces and spices", imageRes = R.drawable.ic_condiment),
            FoodItem(title = "Dips, Spreads & Jams", subtitle = "Spreads", imageRes = R.drawable.ic_jam),
            FoodItem(title = "Prepared Foods & Soups", subtitle = "Ready meals", imageRes = R.drawable.ic_soup),
            FoodItem(title = "Fast Food", subtitle = "Quick meals", imageRes = R.drawable.ic_fastfood),
            FoodItem(title = "Restaurant", subtitle = "Dining out", imageRes = R.drawable.ic_restaurant),
            FoodItem(title = "Chains (by ABC)", subtitle = "Restaurant chains", imageRes = R.drawable.ic_drive_thru),
            FoodItem(title = "Salty Snacks", subtitle = "Chips and snacks", imageRes = R.drawable.ic_salty),
            FoodItem(title = "Sweet Snacks", subtitle = "Candy and sweets", imageRes = R.drawable.ic_sweet),
            FoodItem(title = "Beverages", subtitle = "Drinks", imageRes = R.drawable.ic_beverages),
            FoodItem(title = "Cooking & Baking", subtitle = "Ingredients", imageRes = R.drawable.ic_cooking_baking),
            FoodItem(title = "Baby Food", subtitle = "Infant nutrition", imageRes = R.drawable.ic_baby_food),
            FoodItem(title = "Pet Food", subtitle = "Animal food", imageRes = R.drawable.ic_pet_food)
        )

        updateUI(localList)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}