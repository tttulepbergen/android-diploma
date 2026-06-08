package com.example.scanfit.mainNavigation

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.scanfit.databinding.ItemCalendarDayBinding
import java.util.Calendar

data class CalendarDayItem(
    val calendar: Calendar,
    val dayName: String,
    val dayNumber: String,
    val isSelected: Boolean,
    val isEnabled: Boolean
)

class CalendarAdapter(
    private val onDayClick: (Calendar) -> Unit
) : ListAdapter<CalendarDayItem, CalendarAdapter.DayViewHolder>(DayDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position), onDayClick)
    }

    class DayViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDayItem, onDayClick: (Calendar) -> Unit) {
            binding.tvDayOfWeek.text = item.dayName
            binding.tvDayNumber.text = item.dayNumber

            when {
                item.isSelected && item.isEnabled -> {
                    binding.cardDay.setCardBackgroundColor(Color.parseColor("#5CA2F8"))
                    binding.tvDayOfWeek.setTextColor(Color.WHITE)
                    binding.tvDayNumber.setTextColor(Color.WHITE)
                    binding.tvDayOfWeek.setTypeface(null, Typeface.BOLD)
                    binding.tvDayNumber.setTypeface(null, Typeface.BOLD)
                    binding.root.alpha = 1f
                    binding.cardDay.cardElevation = 4f
                }
                item.isEnabled -> {
                    binding.cardDay.setCardBackgroundColor(Color.parseColor("#F0F2F5"))
                    binding.tvDayOfWeek.setTextColor(Color.parseColor("#9E9E9E"))
                    binding.tvDayNumber.setTextColor(Color.parseColor("#555555"))
                    binding.tvDayOfWeek.setTypeface(null, Typeface.NORMAL)
                    binding.tvDayNumber.setTypeface(null, Typeface.BOLD)
                    binding.root.alpha = 1f
                    binding.cardDay.cardElevation = 0f
                }
                else -> {
                    binding.cardDay.setCardBackgroundColor(Color.parseColor("#F0F2F5"))
                    binding.tvDayOfWeek.setTextColor(Color.parseColor("#BDBDBD"))
                    binding.tvDayNumber.setTextColor(Color.parseColor("#BDBDBD"))
                    binding.tvDayOfWeek.setTypeface(null, Typeface.NORMAL)
                    binding.tvDayNumber.setTypeface(null, Typeface.NORMAL)
                    binding.root.alpha = 0.45f
                    binding.cardDay.cardElevation = 0f
                }
            }

            binding.root.setOnClickListener {
                if (item.isEnabled) onDayClick(item.calendar)
            }
        }
    }

    private class DayDiff : DiffUtil.ItemCallback<CalendarDayItem>() {
        override fun areItemsTheSame(old: CalendarDayItem, new: CalendarDayItem) =
            old.dayNumber == new.dayNumber && old.dayName == new.dayName

        override fun areContentsTheSame(old: CalendarDayItem, new: CalendarDayItem) =
            old == new
    }
}
