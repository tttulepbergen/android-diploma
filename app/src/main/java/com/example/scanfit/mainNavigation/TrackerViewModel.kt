package com.example.scanfit.mainNavigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.scanfit.data.FoodItem
import java.util.Calendar

class TrackerViewModel : ViewModel() {
    //calory
    private val _totalCalories = MutableLiveData(0)
    val totalCalories: LiveData<Int> = _totalCalories

    private val _totalProteins = MutableLiveData(0f)
    val totalProteins: LiveData<Float> = _totalProteins

    private val _totalFat = MutableLiveData(0f)
    val totalFat: LiveData<Float> = _totalFat

    private val _totalCarbs = MutableLiveData(0f)
    val totalCarbs: LiveData<Float> = _totalCarbs
//nutri
    private val _goalCalories = MutableLiveData(2150)
    val goalCalories: LiveData<Int> = _goalCalories

    private val _goalProteins = MutableLiveData(150f)
    val goalProteins: LiveData<Float> = _goalProteins

    private val _goalFat = MutableLiveData(70f)
    val goalFat: LiveData<Float> = _goalFat

    private val _goalCarbs = MutableLiveData(300f)
    val goalCarbs: LiveData<Float> = _goalCarbs
//nutriSetters
    fun setNutritionTotals(
        calories: Int,
        proteins: Float,
        fat: Float,
        carbs: Float
    ) {
        _totalCalories.value = calories
        _totalProteins.value = proteins
        _totalFat.value = fat
        _totalCarbs.value = carbs
    }

    fun setNutritionGoals(
        calories: Int?,
        proteins: Float?,
        fat: Float?,
        carbs: Float?
    ) {
        _goalCalories.value = calories ?: 0
        _goalProteins.value = proteins ?: 0f
        _goalFat.value = fat ?: 0f
        _goalCarbs.value = carbs ?: 0f
    }

    fun addFoodData(item: FoodItem) {
        val cal = item.calories?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        _totalCalories.value = (_totalCalories.value ?: 0) + cal

        fun String?.toCleanFloat(): Float {
            if (this == null) return 0f
            val cleanString = this.replace(',', '.').filter { it.isDigit() || it == '.' }
            return cleanString.toFloatOrNull() ?: 0f
        }

        _totalProteins.value = (_totalProteins.value ?: 0f) + item.proteins.toCleanFloat()
        _totalFat.value = (_totalFat.value ?: 0f) + item.fat.toCleanFloat()
        _totalCarbs.value = (_totalCarbs.value ?: 0f) + item.carbs.toCleanFloat()
    }
//water
    private val _waterGlasses = MutableLiveData(0)
    val waterGlasses: LiveData<Int> = _waterGlasses

    private val _waterGoalMl = MutableLiveData(0)
    val waterGoalMl: LiveData<Int> = _waterGoalMl

    fun addWaterGlass() {
        val current = _waterGlasses.value ?: 0
        if (current < 8) {
            _waterGlasses.value = current + 1
        }
    }

    fun removeWaterGlass() {
        val current = _waterGlasses.value ?: 0
        if (current > 0) {
            _waterGlasses.value = current - 1
        }
    }

    fun setWaterGlasses(glasses: Int) {
        _waterGlasses.value = glasses.coerceAtLeast(0)
    }

    fun setWaterGoalMl(goalMl: Int) {
        _waterGoalMl.value = goalMl.coerceAtLeast(0)
    }
//date
    private val _selectedDate = MutableLiveData(Calendar.getInstance())
    val selectedDate: LiveData<Calendar> = _selectedDate

    private val _firstAvailableDate = MutableLiveData<Calendar?>()
    val firstAvailableDate: LiveData<Calendar?> = _firstAvailableDate

    fun setSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar
    }

    fun setFirstAvailableDate(calendar: Calendar?) {
        _firstAvailableDate.value = calendar?.normalizedCopy()
        val selected = _selectedDate.value ?: return
        if (!canSelectDate(selected)) {
            _selectedDate.value = _firstAvailableDate.value?.normalizedCopy()
                ?: selected.normalizedCopy()
        }
    }

    fun canSelectDate(calendar: Calendar): Boolean {
        val firstDate = _firstAvailableDate.value ?: return true
        return !calendar.normalizedCopy().before(firstDate)
    }

    private fun Calendar.normalizedCopy(): Calendar {
        return (clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}