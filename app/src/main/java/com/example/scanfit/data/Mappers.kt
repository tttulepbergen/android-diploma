// Файл: app/src/main/java/com/example/scanfit/data/Mappers.kt
package com.example.scanfit.data

import com.example.scanfit.model.Product

fun Product.toFoodItem(): FoodItem {
    val nutris = this.nutriments
    val estimated = this.nutrimentsEstimated

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
        vitaminD = formatMicrograms(nutris?.vitaminDServing ?: nutris?.vitaminD100g ?: estimated?.vitaminDServing ?: estimated?.vitaminD100g),
        vitaminB12 = formatMicrograms(nutris?.vitaminB12Serving ?: nutris?.vitaminB12100g ?: estimated?.vitaminB12Serving ?: estimated?.vitaminB12100g),
        vitaminC = formatMilligrams(nutris?.vitaminCServing ?: nutris?.vitaminC100g ?: estimated?.vitaminCServing ?: estimated?.vitaminC100g),
        vitaminA = formatMicrograms(nutris?.vitaminAServing ?: nutris?.vitaminA100g ?: estimated?.vitaminAServing ?: estimated?.vitaminA100g),
        vitaminB6 = formatMilligrams(nutris?.vitaminB6Serving ?: nutris?.vitaminB6100g ?: estimated?.vitaminB6Serving ?: estimated?.vitaminB6100g),
        vitaminB9 = formatMicrograms(nutris?.vitaminB9Serving ?: nutris?.vitaminB9100g ?: nutris?.folatesServing ?: nutris?.folates100g ?: estimated?.vitaminB9Serving ?: estimated?.vitaminB9100g ?: estimated?.folatesServing ?: estimated?.folates100g),
        vitaminE = formatMilligrams(nutris?.vitaminEServing ?: nutris?.vitaminE100g ?: estimated?.vitaminEServing ?: estimated?.vitaminE100g),
        description = this.quantity ?: "",
        ingredients = this.ingredientsText ?: this.ingredientsTextEn ?: ""
    )
}

private fun formatMilligrams(valueInGrams: Double?): String {
    return "${((valueInGrams ?: 0.0) * 1000).formatAmount()} mg"
}

private fun formatMicrograms(valueInGrams: Double?): String {
    return "${((valueInGrams ?: 0.0) * 1_000_000).formatAmount()} mcg"
}

private fun Double.formatAmount(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
    }
}
