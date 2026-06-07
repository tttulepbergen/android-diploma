package com.example.scanfit.mainNavigation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanfit.R
import com.example.scanfit.adapters.FoodAdapter
import com.example.scanfit.data.AppDatabase
import com.example.scanfit.data.FoodItem
import com.example.scanfit.databinding.FragmentFavouritesBinding
import com.example.scanfit.data.toFavoriteProduct
import com.example.scanfit.data.toFoodItem
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment(R.layout.fragment_favourites) {

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val gson = GsonBuilder().serializeNulls().create()
    private lateinit var foodAdapter: FoodAdapter
    private var allItems: List<FoodItem> = emptyList()
    private var currentFilter: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouritesBinding.bind(view)
        setupRecyclerView()
        refresh()

        binding.chipAll.setOnCheckedChangeListener { _, checked -> if (checked) applyFilter(null) }
        binding.chipScan.setOnCheckedChangeListener { _, checked -> if (checked) applyFilter("scan") }
        binding.chipOpenfoodfacts.setOnCheckedChangeListener { _, checked -> if (checked) applyFilter("openfoodfacts") }

        binding.btnClearAll?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                database.productDao().deleteAllFavorites()
                showItems(emptyList())
            }
        }
    }

    private fun refresh() {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = loadItems()
            showItems(items)
        }
    }

    private suspend fun loadItems(): List<FoodItem> {
        val token = SessionManager(requireContext()).fetchAuthToken()
        if (!token.isNullOrBlank()) {
            val backendItems = loadFromBackend(token)
            if (!backendItems.isNullOrEmpty()) return backendItems
        }
        return database.productDao().getAllFavoritesOnce().map { it.toFoodItem() }
    }

    private suspend fun loadFromBackend(token: String): List<FoodItem>? {
        return runCatching {
            val response = NetworkClient.userApiService.getUserLikes(token)
            val likeItems = response.data ?: return@runCatching null

            likeItems.mapNotNull { like ->
                if (like.productData.isBlank()) return@mapNotNull null
                runCatching {
                    val foodItem = gson.fromJson(like.productData, FoodItem::class.java)
                    val resolvedSource = like.source.ifBlank {
                        foodItem?.source?.ifBlank { "openfoodfacts" } ?: "openfoodfacts"
                    }
                    foodItem?.copy(isFavorite = true, source = resolvedSource)?.also { item ->
                        database.productDao().insertFavorite(item.toFavoriteProduct(like.id))
                    }
                }.getOrNull()
            }
        }.onFailure { e ->
            Log.e("FavoritesFragment", "Error loading likes from backend: ${e.message}")
        }.getOrNull()
    }

    private fun showItems(items: List<FoodItem>) {
        if (_binding == null) return
        allItems = items
        val filtered = if (currentFilter == null) items else items.filter { it.source == currentFilter }
        if (items.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvFavorites.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvFavorites.visibility = View.VISIBLE
            foodAdapter.updateList(filtered)
        }
    }

    private fun applyFilter(source: String?) {
        currentFilter = source
        val filtered = if (source == null) allItems else allItems.filter { it.source == source }
        foodAdapter.updateList(filtered)
    }

    private fun handleUnfavorite(item: FoodItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = SessionManager(requireContext()).fetchAuthToken()
            if (!token.isNullOrBlank()) {
                val existing = database.productDao().getFavoriteById(item.title)
                val backendId = existing?.backendId ?: 0L
                if (backendId > 0) {
                    runCatching {
                        NetworkClient.userApiService.deleteLike(token, backendId)
                    }.onFailure { e ->
                        Log.e("FavoritesFragment", "Error deleting like from backend: ${e.message}")
                    }
                }
            }
            database.productDao().deleteFavoriteById(item.title)
            refresh()
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            items = emptyList(),
            showFavoriteIcon = true,
            onItemClick = { item ->
                val bundle = bundleOf("foodItem" to item)
                findNavController().navigate(
                    R.id.action_favoritesFragment_to_productDetailFragment,
                    bundle
                )
            },
            onFavoriteClick = { item -> handleUnfavorite(item) }
        )
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = foodAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
