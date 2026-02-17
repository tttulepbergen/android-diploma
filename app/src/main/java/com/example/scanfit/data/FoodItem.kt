package com.example.scanfit.data

import java.io.Serializable

// Основной объект, соответствующий корню JSON
data class HealthData(
    val categories: List<HealthCategory>
) : Serializable

data class HealthCategory(
    val category_id: String,
    val category_name: String,
    val items: List<DietItem>
) : Serializable

data class DietItem(
    val id: String,
    val name: String,
    val ui_type: String, // "selection_modal", "severity_slider", "simple_toggle"
    var isSelected: Boolean = false, // Новое поле для отслеживания выбора
    val max_levels: Int? = 2, // Добавьте эту строку. По умолчанию будет 2 уровня.
    val sub_options: List<SubOption>? = null,
    val triggers: List<String>? = null,
    var category_name: String? = null // Поле для хранения имени категории после парсинга
) : Serializable

data class SubOption(
    val id: String,
    val name: String,
    val ui_type: String? = "severity_slider", // По умолчанию ползунок
    val triggers: List<String>? = null,
    val max_levels: Int? = null    // И это
) : Serializable

// Модель для продуктов (сканирование)
data class FoodItem(
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val calories: String? = "0",
    val grade: String? = "B",
    var isFavorite: Boolean = false,
    val proteins: String = "0g",
    val fat: String = "0g",
    val carbs: String = "0g",
    val description: String = "",
    val cholesterol: String? = "0mg",
    val sodium: String? = "0mg",
    val sugars: String? = "0g",
    val fiber: String? = "0g"
) : Serializable