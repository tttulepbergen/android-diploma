package com.example.scanfit.mainNavigation

import android.os.Bundle
import android.util.Log
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
import com.example.scanfit.data.FoodItem
import com.example.scanfit.databinding.FragmentRecentBinding
import com.example.scanfit.data.toFavoriteProduct
import com.example.scanfit.data.toFoodItem
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch

class RecentFragment : Fragment(R.layout.fragment_recent) {

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private lateinit var foodAdapter: FoodAdapter
    private val gson = GsonBuilder().serializeNulls().create()
    private var _binding: FragmentRecentBinding? = null
    private var allItems: List<FoodItem> = emptyList()
    private var currentFilter: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecentBinding.bind(view)
        val binding = _binding!!

        foodAdapter = FoodAdapter(
            items = emptyList(),
            onItemClick = { selectedItem ->
                val bundle = bundleOf("foodItem" to selectedItem)
                findNavController().navigate(
                    R.id.action_recentFragment_to_productDetailFragment,
                    bundle
                )
            },
            onFavoriteClick = { clickedItem -> handleFavoriteAction(clickedItem) },
            showFavoriteIcon = true
        )

        binding.rvRecent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
        }

        binding.chipAll.setOnCheckedChangeListener { _, checked -> if (checked) applyFilter(null) }
        binding.chipScan.setOnCheckedChangeListener { _, checked -> if (checked) applyFilter("scan") }
        binding.chipOpenfoodfacts.setOnCheckedChangeListener { _, checked -> if (checked) applyFilter("openfoodfacts") }

        binding.btnClearAll.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                database.productDao().clearRecent()
                showItems(emptyList())
            }
        }

        loadRecent()
    }

    private fun loadRecent() {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = resolveItems()
            showItems(items)
        }
    }

    private suspend fun resolveItems(): List<FoodItem> {
        val token = SessionManager(requireContext()).fetchAuthToken()
        if (!token.isNullOrBlank()) {
            val backendItems = loadFromBackend(token)
            if (!backendItems.isNullOrEmpty()) return backendItems
        }
        val favoritesIds = database.productDao().getAllFavoritesOnce().map { it.id }.toSet()
        return database.productDao().getAllRecentOnce().map { recent ->
            recent.toFoodItem(isFavorite = favoritesIds.contains(recent.title))
        }
    }

    private suspend fun loadFromBackend(token: String): List<FoodItem>? {
        return runCatching {
            val response = NetworkClient.userApiService.getUserHistory(token)
            val historyItems = response.data ?: return@runCatching null

            val favoritesIds = database.productDao().getAllFavoritesOnce().map { it.id }.toSet()

            historyItems.mapNotNull { historyItem ->
                if (historyItem.productData.isBlank()) return@mapNotNull null
                runCatching {
                    val foodItem = gson.fromJson(historyItem.productData, FoodItem::class.java)
                    val resolvedSource = historyItem.source.ifBlank {
                        foodItem?.source?.ifBlank { "openfoodfacts" } ?: "openfoodfacts"
                    }
                    foodItem?.copy(
                        isFavorite = favoritesIds.contains(foodItem.title),
                        source = resolvedSource
                    )
                }.getOrNull()
            }.distinctBy { it.title }
        }.onFailure { e ->
            Log.e("RecentFragment", "Error loading history from backend: ${e.message}")
        }.getOrNull()
    }

    private fun showItems(items: List<FoodItem>) {
        val binding = _binding ?: return
        allItems = items
        val filtered = if (currentFilter == null) items else items.filter { it.source == currentFilter }
        if (items.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvRecent.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvRecent.visibility = View.VISIBLE
            foodAdapter.updateList(filtered)
        }
    }

    private fun applyFilter(source: String?) {
        currentFilter = source
        val filtered = if (source == null) allItems else allItems.filter { it.source == source }
        val binding = _binding ?: return
        if (filtered.isEmpty() && allItems.isNotEmpty()) {
            foodAdapter.updateList(emptyList())
        } else {
            foodAdapter.updateList(filtered)
        }
    }

    private fun handleFavoriteAction(item: FoodItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (item.isFavorite) {
                database.productDao().insertFavorite(item.toFavoriteProduct())
                Toast.makeText(context, "Saved to favourites", Toast.LENGTH_SHORT).show()
            } else {
                database.productDao().deleteFavoriteById(item.title)
                Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
