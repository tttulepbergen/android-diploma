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
    val timestamp: Long = System.currentTimeMillis()
) : Serializable