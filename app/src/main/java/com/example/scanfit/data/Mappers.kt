// Файл: app/src/main/java/com/example/scanfit/data/Mappers.kt
package com.example.scanfit.data

import com.example.scanfit.model.Product

fun Product.toFoodItem(): FoodItem {
    val nutris = this.nutriments

    val kcal = nutris?.energyKcalServing ?: nutris?.energyKcal100g ?: 0.0

    return FoodItem(
        title = this.productName ?: "Unknown Product",
        subtitle = this.brands ?: "Brand",
        imageUrl = this.imageUrl,
        calories = "${kcal.toInt()} cal",
        grade = this.nutriscoreGrade?.uppercase() ?: "B",

        proteins = "${nutris?.proteinsServing ?: nutris?.proteins100g ?: 0.0}g",
        fat = "${nutris?.fatServing ?: nutris?.fat100g ?: 0.0}g",
        carbs = "${nutris?.carbohydratesServing ?: nutris?.carbohydrates100g ?: 0.0}g",

        cholesterol = "${nutris?.cholesterolServing ?: nutris?.cholesterol100g ?: 0.0}mg",
        sodium = "${nutris?.sodiumServing ?: nutris?.sodium100g ?: 0.0}mg",
        sugars = "${nutris?.sugarsServing ?: nutris?.sugars100g ?: 0.0}g",
        fiber = "${nutris?.fiberServing ?: nutris?.fiber100g ?: 0.0}g",
        description = this.quantity ?: ""
    )
}