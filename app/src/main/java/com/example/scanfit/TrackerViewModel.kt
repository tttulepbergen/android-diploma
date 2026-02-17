package com.example.scanfit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scanfit.data.FoodItem

class TrackerViewModel : ViewModel() {
    private val _totalCalories = MutableLiveData(0)
    val totalCalories: LiveData<Int> = _totalCalories

    private val _totalProteins = MutableLiveData(0f)
    val totalProteins: LiveData<Float> = _totalProteins

    private val _totalFat = MutableLiveData(0f)
    val totalFat: LiveData<Float> = _totalFat

    private val _totalCarbs = MutableLiveData(0f)
    val totalCarbs: LiveData<Float> = _totalCarbs

    fun addFoodData(item: FoodItem) {
        // Калории
        val cal = item.calories?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        _totalCalories.value = (_totalCalories.value ?: 0) + cal

        // БЖУ: заменяем запятую на точку перед парсингом
        fun String?.toCleanFloat(): Float {
            if (this == null) return 0f
            val cleanString = this.replace(',', '.').filter { it.isDigit() || it == '.' }
            return cleanString.toFloatOrNull() ?: 0f
        }

        _totalProteins.value = (_totalProteins.value ?: 0f) + item.proteins.toCleanFloat()
        _totalFat.value = (_totalFat.value ?: 0f) + item.fat.toCleanFloat()
        _totalCarbs.value = (_totalCarbs.value ?: 0f) + item.carbs.toCleanFloat()
    }

    // В TrackerViewModel.kt
    private val _waterGlasses = MutableLiveData(0)
    val waterGlasses: LiveData<Int> = _waterGlasses

    fun addWaterGlass() {
        val current = _waterGlasses.value ?: 0
        if (current < 8) { // Например, максимум 8 стаканов
            _waterGlasses.value = current + 1
        }
    }

    // В TrackerViewModel.kt
    private val _selectedDate = MutableLiveData(java.util.Calendar.getInstance())
    val selectedDate: LiveData<java.util.Calendar> = _selectedDate

    fun setSelectedDate(calendar: java.util.Calendar) {
        _selectedDate.value = calendar
        // Здесь позже можно добавить загрузку данных из базы именно за этот день
    }
}