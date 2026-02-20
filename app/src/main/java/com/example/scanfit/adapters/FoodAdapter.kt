package com.example.scanfit.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.scanfit.R
import com.example.scanfit.databinding.ItemFoodBinding
import com.example.scanfit.data.FoodItem

class FoodAdapter(
    private var items: List<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit,
    private val onFavoriteClick: (FoodItem) -> Unit = {},
    private val showFavoriteIcon: Boolean = false,
    private val showDetails: Boolean = true
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FoodItem) {
            binding.tvFoodTitle.text = item.title

            if (showDetails) {
                binding.tvFoodSubtitle.visibility = View.VISIBLE
                binding.tvCalories.visibility = View.VISIBLE
                binding.tvFoodSubtitle.text = item.subtitle ?: ""
                binding.tvCalories.text = item.calories ?: "0 cal"
            } else {
                binding.tvFoodSubtitle.visibility = View.GONE
                binding.tvCalories.visibility = View.GONE
            }

            if (showFavoriteIcon) {
                binding.ivFavorite.visibility = View.VISIBLE
                val iconRes = if (item.isFavorite) R.drawable.ic_favourites_liked else R.drawable.ic_favourites
                binding.ivFavorite.setImageResource(iconRes)

                val color = if (item.isFavorite) "#FF4B4B" else "#BDBDBD"
                binding.ivFavorite.setColorFilter(Color.parseColor(color))

                binding.ivFavorite.setOnClickListener {
                    item.isFavorite = !item.isFavorite
                    onFavoriteClick(item)
                    notifyItemChanged(adapterPosition)
                }
            } else {
                binding.ivFavorite.visibility = View.GONE
            }

            when {
                !item.imageUrl.isNullOrEmpty() -> {
                    binding.ivFoodIcon.load(item.imageUrl) {
                        placeholder(R.drawable.ic_launcher_foreground)
                        error(R.drawable.ic_launcher_foreground)
                    }
                }
                item.imageRes != null -> {
                    binding.ivFoodIcon.setImageResource(item.imageRes)
                }
                else -> {
                    binding.ivFoodIcon.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    fun updateList(newItems: List<FoodItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}