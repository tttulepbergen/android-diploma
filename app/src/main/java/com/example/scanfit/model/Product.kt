package com.example.scanfit.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("code") val code: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("nutriscore_grade") val nutriscoreGrade: String?,

    @SerializedName("nutriments") val nutriments: NutrimentsData?,

    @SerializedName("energy-kcal_100g") val energyKcal100g: Double?,
    @SerializedName("serving_size") val servingSize: String?,
    @SerializedName("quantity") val quantity: String?
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
    @SerializedName("cholesterol_serving") val cholesterolServing: Double?
)