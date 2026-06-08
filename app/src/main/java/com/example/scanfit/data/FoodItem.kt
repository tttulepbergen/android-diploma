package com.example.scanfit.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Основной объект, соответствующий корню JSON выв
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
    val description: String? = null,
    val ui_type: String,
    var isSelected: Boolean = false,
    val max_levels: Int? = 2,
    val sub_options: List<SubOption>? = null,
    val triggers: List<String>? = null,
    var category_name: String? = null
) : Serializable

data class SubOption(
    val id: String,
    val name: String,
    val ui_type: String? = "severity_slider",
    val triggers: List<String>? = null,
    val max_levels: Int? = null
) : Serializable

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
    val fiber: String? = "0g",
    val vitaminD: String? = "0 mcg",
    val vitaminB12: String? = "0 mcg",
    val vitaminC: String? = "0 mg",
    val vitaminA: String? = "0 mcg",
    val vitaminB6: String? = "0 mg",
    val vitaminB9: String? = "0 mcg",
    val vitaminE: String? = "0 mg",
    @SerializedName("ingredients_text")
    val ingredients: String?,
    val source: String = "openfoodfacts"
    ) : Serializable
