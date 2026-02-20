package com.example.scanfit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete
@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProduct(barcode: String): ProductEntity?

    @Query("SELECT * FROM favorites_table")
    fun getAllFavorites(): Flow<List<FavoriteProduct>> // Используем FavoriteProduct

    @Query("SELECT * FROM favorites_table WHERE id = :id LIMIT 1")
    suspend fun getFavoriteById(id: String): FavoriteProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteProduct)

    @Query("DELETE FROM favorites_table") // Replace with your actual table name
    suspend fun deleteAllFavorites()

    @Delete
    suspend fun deleteFavorite(item: FavoriteProduct)

    @Query("SELECT * FROM favorites_table")
    suspend fun getAllFavoritesOnce(): List<FavoriteProduct>

    @Query("DELETE FROM favorites_table WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(product: RecentProduct)

    @Query("SELECT * FROM recent_products ORDER BY timestamp DESC LIMIT 20")
    fun getAllRecent(): Flow<List<RecentProduct>>

    @Query("DELETE FROM recent_products")
    suspend fun clearRecent()
}