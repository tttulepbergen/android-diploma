package com.example.scanfit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.data.FavoriteProduct
import com.example.scanfit.data.FoodItem
import com.example.scanfit.databinding.FragmentRecentBinding
import kotlinx.coroutines.launch

class RecentFragment : Fragment(R.layout.fragment_recent) {

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private lateinit var foodAdapter: FoodAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentRecentBinding.bind(view)

        foodAdapter = FoodAdapter(
            items = emptyList(),
            onItemClick = { selectedItem ->
                val bundle = bundleOf("foodItem" to selectedItem)
                findNavController().navigate(R.id.action_recentFragment_to_productDetailFragment, bundle)
            },
            onFavoriteClick = { clickedItem ->
                handleFavoriteAction(clickedItem)
            },
            showFavoriteIcon = true
        )

        binding.rvRecent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
        }

        binding.btnClearAll.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                database.productDao().clearRecent()
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            database.productDao().getAllRecent().collect { recentList ->

                val favoritesIds = database.productDao().getAllFavoritesOnce().map { it.id }.toSet()

                if (recentList.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvRecent.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvRecent.visibility = View.VISIBLE

                    val items = recentList.map { recent ->
                        FoodItem(
                            title = recent.title,
                            subtitle = recent.subtitle ?: "",
                            imageUrl = recent.imageUrl ?: "",
                            calories = recent.calories ?: "0 cal",
                            grade = recent.grade ?: "B",
                            isFavorite = favoritesIds.contains(recent.title)
                        )
                    }
                    foodAdapter.updateList(items)
                }
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
                    grade = item.grade
                )
                database.productDao().insertFavorite(entity)
                Toast.makeText(context, "Saved to favourites", Toast.LENGTH_SHORT).show()
            } else {
                database.productDao().deleteFavoriteById(id)
                Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show()
            }
        }
    }
}