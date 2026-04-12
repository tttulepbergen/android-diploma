package com.example.scanfit.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isVisible
import com.example.scanfit.R
import com.example.scanfit.data.DietItem

class DietAdapter(
    private var items: List<Any>,
    private val onDietClick: (DietItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DIET = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_DIET
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(inflater.inflate(R.layout.item_category_header, parent, false))
        } else {
            DietViewHolder(inflater.inflate(R.layout.item_diet_chip, parent, false))
        }
    }




    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is HeaderViewHolder && item is String) {
            holder.title.text = item
        }
        else if (holder is DietViewHolder && item is DietItem) {
            val diet = item
            holder.nameText.text = diet.name
            holder.descriptionText.text = diet.description
            holder.descriptionText.isVisible = !diet.description.isNullOrBlank()
            holder.arrowIcon.visibility = if (diet.category_name == "disease") View.VISIBLE else View.GONE

            if (diet.isSelected) {
                holder.itemView.setBackgroundResource(R.drawable.bg_diet_chip_selected)
                holder.nameText.setTextColor(Color.parseColor("#589BFF"))
                holder.descriptionText.setTextColor(Color.parseColor("#6B84C6"))
            } else {
                holder.itemView.setBackgroundResource(R.drawable.bg_diet_chip_normal)
                holder.nameText.setTextColor(Color.BLACK)
                holder.descriptionText.setTextColor(Color.parseColor("#7B8494"))
            }

            holder.itemView.setOnClickListener {
                onDietClick(diet)
            }
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_header_title)
    }

    class DietViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.dietName)
        val descriptionText: TextView = view.findViewById(R.id.dietDescription)
        val arrowIcon: ImageView = view.findViewById(R.id.arrowIcon)
    }

    fun updateData(newItems: List<Any>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
