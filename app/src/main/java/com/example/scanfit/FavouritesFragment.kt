package com.example.scanfit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.databinding.FragmentFavouritesBinding
import kotlinx.coroutines.launch
import com.example.scanfit.data.FoodItem
import com.example.scanfit.data.ProductDao
import androidx.navigation.fragment.findNavController
class FavoritesFragment : Fragment(R.layout.fragment_favourites) {

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouritesBinding.bind(view)

        val database = AppDatabase.getDatabase(requireContext())
        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            database.productDao().getAllFavorites().collect { favoriteList ->
                if (favoriteList.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvFavorites.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvFavorites.visibility = View.VISIBLE

                    val foodItems = favoriteList.map { favorite ->
                        FoodItem(
                            title = favorite.productName,
                            subtitle = favorite.name ?: "Unknown Brand",                            imageUrl = favorite.imageUrl,
                            calories = favorite.calories,
                            grade = favorite.grade,
                            isFavorite = true
                        )
                    }

                    binding.rvFavorites.adapter = FoodAdapter(
                        items = foodItems,
                        showFavoriteIcon = true,
                        onItemClick = { item ->
                            val bundle = androidx.core.os.bundleOf("foodItem" to item)
                            findNavController().navigate(
                                R.id.action_favoritesFragment_to_productDetailFragment,
                                bundle
                            )
                        },
                        onFavoriteClick = { item ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                database.productDao().deleteFavoriteById(item.title)
                            }
                        }
                    )
                }
            }
        }

        binding.btnClearAll?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                database.productDao().deleteAllFavorites()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}