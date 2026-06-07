package com.example.scanfit.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val ingredients: String,
    val sugar: Double?,
    val fat: Double?,
    val salt: Double?,
    val calories: Double?,
    val recommendation: String
)

@Entity(tableName = "favorites_table")
data class FavoriteProduct(
    @PrimaryKey val id: String,
    val productName: String,
    val name: String,
    val imageUrl: String?,
    val calories: String?,
    val grade: String?,
    val ingredients: String? = "",
    val proteins: String? = "0g",
    val fat: String? = "0g",
    val carbs: String? = "0g",
    val description: String? = "",
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
    val backendId: Long = 0,
    val source: String = "openfoodfacts"
)
