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
    val grade: String?
)