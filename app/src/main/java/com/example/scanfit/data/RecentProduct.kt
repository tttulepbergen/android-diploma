package com.example.scanfit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "recent_products")
data class RecentProduct(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val calories: String?,
    val grade: String?,
    val timestamp: Long = System.currentTimeMillis(),
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
    val source: String = "openfoodfacts"
) : Serializable
