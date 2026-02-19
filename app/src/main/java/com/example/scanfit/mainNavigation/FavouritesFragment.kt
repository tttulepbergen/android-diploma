package com.example.scanfit.mainNavigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.databinding.FragmentFavouritesBinding
import kotlinx.coroutines.launch
import com.example.scanfit.data.FoodItem
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import com.example.scanfit.adapters.FoodAdapter
import com.example.scanfit.R

class FavoritesFragment : Fragment(R.layout.fragment_favourites) {

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouritesBinding.bind(view)

        val database = AppDatabase.getDatabase(requireContext())
        setupRecyclerView()

        // Слушаем изменения в базе данных через Flow
        viewLifecycleOwner.lifecycleScope.launch {
            database.productDao().getAllFavorites().collect { favoriteList ->
                if (favoriteList.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvFavorites.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvFavorites.visibility = View.VISIBLE

                    // Маппим данные из Entity базы данных в модель для UI
                    val foodItems = favoriteList.map { favorite ->
                        FoodItem(
                            title = favorite.name,
                            subtitle = "",
                            imageUrl = favorite.imageUrl,
                            calories = favorite.calories,
                            grade = favorite.grade,
                            isFavorite = true // В этом экране все элементы — избранные
                        )
                    }

                    // Передаем true в параметр showFavoriteIcon, так как здесь нам НУЖНЫ сердечки
                    binding.rvFavorites.adapter = FoodAdapter(
                        items = foodItems,
                        showFavoriteIcon = true, // Показываем иконку (сердечко)
                        onItemClick = { item ->
                            val bundle = bundleOf("foodItem" to item)
                            findNavController().navigate(
                                R.id.action_favoritesFragment_to_productDetailFragment,
                                bundle
                            )                        },
                        onFavoriteClick = { item ->
                            // Удаление из базы при нажатии на сердечко
                            viewLifecycleOwner.lifecycleScope.launch {
                                val favoriteToDelete = favoriteList.find { it.name == item.title }
                                favoriteToDelete?.let {
                                    database.productDao().deleteFavorite(it)
                                }
                            }
                        }
                    )
                }
            }
        }

        // Логика для кнопки "Clear All" (если она есть в XML)
        binding.btnClearAll?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                database.productDao().deleteAllFavorites() // Call the "Clear All" method
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
    }

    // Метод loadFavorites() тебе больше не нужен, так как Flow в onViewCreated
    // сам обновляет список автоматически при любых изменениях в базе.

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}