package com.example.scanfit.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("code") val code: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("nutriscore_grade") val nutriscoreGrade: String?,

    @SerializedName("nutriments") val nutriments: NutrimentsData?,
    @SerializedName("nutriments_estimated") val nutrimentsEstimated: NutrimentsData?,

    @SerializedName("energy-kcal_100g") val energyKcal100g: Double?,
    @SerializedName("serving_size") val servingSize: String?,
    @SerializedName("quantity") val quantity: String?,

    @SerializedName("ingredients_text") val ingredientsText: String?,
    @SerializedName("ingredients_text_en") val ingredientsTextEn: String?

)

data class NutrimentsData(
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double?,
    @SerializedName("energy-kcal_serving") val energyKcalServing: Double?,
    @SerializedName("proteins_100g") val proteins100g: Double?,
    @SerializedName("proteins_serving") val proteinsServing: Double?,
    @SerializedName("fat_100g") val fat100g: Double?,
    @SerializedName("fat_serving") val fatServing: Double?,
    @SerializedName("carbohydrates_100g") val carbohydrates100g: Double?,
    @SerializedName("carbohydrates_serving") val carbohydratesServing: Double?,
    @SerializedName("sugars_100g") val sugars100g: Double?,
    @SerializedName("sugars_serving") val sugarsServing: Double?,
    @SerializedName("fiber_100g") val fiber100g: Double?,
    @SerializedName("fiber_serving") val fiberServing: Double?,
    @SerializedName("sodium_100g") val sodium100g: Double?,
    @SerializedName("sodium_serving") val sodiumServing: Double?,
    @SerializedName("cholesterol_100g") val cholesterol100g: Double?,
    @SerializedName("cholesterol_serving") val cholesterolServing: Double?,
    @SerializedName("vitamin-d_100g") val vitaminD100g: Double?,
    @SerializedName("vitamin-d_serving") val vitaminDServing: Double?,
    @SerializedName("vitamin-b12_100g") val vitaminB12100g: Double?,
    @SerializedName("vitamin-b12_serving") val vitaminB12Serving: Double?,
    @SerializedName("vitamin-c_100g") val vitaminC100g: Double?,
    @SerializedName("vitamin-c_serving") val vitaminCServing: Double?,
    @SerializedName("vitamin-a_100g") val vitaminA100g: Double?,
    @SerializedName("vitamin-a_serving") val vitaminAServing: Double?,
    @SerializedName("vitamin-b6_100g") val vitaminB6100g: Double?,
    @SerializedName("vitamin-b6_serving") val vitaminB6Serving: Double?,
    @SerializedName("vitamin-b9_100g") val vitaminB9100g: Double?,
    @SerializedName("vitamin-b9_serving") val vitaminB9Serving: Double?,
    @SerializedName("folates_100g") val folates100g: Double?,
    @SerializedName("folates_serving") val folatesServing: Double?,
    @SerializedName("vitamin-e_100g") val vitaminE100g: Double?,
    @SerializedName("vitamin-e_serving") val vitaminEServing: Double?
)
