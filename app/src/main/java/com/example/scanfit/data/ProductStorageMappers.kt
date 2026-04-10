package com.example.scanfit.data

fun FoodItem.toFavoriteProduct(): FavoriteProduct {
    return FavoriteProduct(
        id = title,
        productName = title,
        name = subtitle ?: "",
        imageUrl = imageUrl,
        calories = calories,
        grade = grade,
        ingredients = ingredients,
        proteins = proteins,
        fat = fat,
        carbs = carbs,
        description = description,
        cholesterol = cholesterol,
        sodium = sodium,
        sugars = sugars,
        fiber = fiber,
        vitaminD = vitaminD,
        vitaminB12 = vitaminB12,
        vitaminC = vitaminC,
        vitaminA = vitaminA,
        vitaminB6 = vitaminB6,
        vitaminB9 = vitaminB9,
        vitaminE = vitaminE
    )
}

fun FoodItem.toRecentProduct(timestamp: Long = System.currentTimeMillis()): RecentProduct {
    return RecentProduct(
        id = title,
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        calories = calories,
        grade = grade,
        timestamp = timestamp,
        ingredients = ingredients,
        proteins = proteins,
        fat = fat,
        carbs = carbs,
        description = description,
        cholesterol = cholesterol,
        sodium = sodium,
        sugars = sugars,
        fiber = fiber,
        vitaminD = vitaminD,
        vitaminB12 = vitaminB12,
        vitaminC = vitaminC,
        vitaminA = vitaminA,
        vitaminB6 = vitaminB6,
        vitaminB9 = vitaminB9,
        vitaminE = vitaminE
    )
}

fun FavoriteProduct.toFoodItem(): FoodItem {
    return FoodItem(
        title = productName,
        subtitle = name,
        imageUrl = imageUrl,
        calories = calories,
        grade = grade,
        isFavorite = true,
        proteins = proteins ?: "0g",
        fat = fat ?: "0g",
        carbs = carbs ?: "0g",
        description = description ?: "",
        cholesterol = cholesterol,
        sodium = sodium,
        sugars = sugars,
        fiber = fiber,
        vitaminD = vitaminD,
        vitaminB12 = vitaminB12,
        vitaminC = vitaminC,
        vitaminA = vitaminA,
        vitaminB6 = vitaminB6,
        vitaminB9 = vitaminB9,
        vitaminE = vitaminE,
        ingredients = ingredients
    )
}

fun RecentProduct.toFoodItem(isFavorite: Boolean): FoodItem {
    return FoodItem(
        title = title,
        subtitle = subtitle ?: "",
        imageUrl = imageUrl ?: "",
        calories = calories ?: "0 cal",
        grade = grade ?: "B",
        isFavorite = isFavorite,
        proteins = proteins ?: "0g",
        fat = fat ?: "0g",
        carbs = carbs ?: "0g",
        description = description ?: "",
        cholesterol = cholesterol,
        sodium = sodium,
        sugars = sugars,
        fiber = fiber,
        vitaminD = vitaminD,
        vitaminB12 = vitaminB12,
        vitaminC = vitaminC,
        vitaminA = vitaminA,
        vitaminB6 = vitaminB6,
        vitaminB9 = vitaminB9,
        vitaminE = vitaminE,
        ingredients = ingredients
    )
}
