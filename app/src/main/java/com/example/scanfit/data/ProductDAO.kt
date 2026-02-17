package com.example.scanfit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete
@Dao
interface ProductDao {
    // Работа с обычными продуктами
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProduct(barcode: String): ProductEntity?

    // Работа с избранным (Исправлено)
    @Query("SELECT * FROM favorites_table")
    fun getAllFavorites(): Flow<List<FavoriteProduct>> // Используем FavoriteProduct

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteProduct)

    @Query("DELETE FROM favorites_table") // Replace with your actual table name
    suspend fun deleteAllFavorites()

    // Удаление одного избранного продукта по entity
    @Delete
    suspend fun deleteFavorite(item: FavoriteProduct)

    // Разовое получение всех избранных (для инициализации списка)
    @Query("SELECT * FROM favorites_table")
    suspend fun getAllFavoritesOnce(): List<FavoriteProduct>

    // Удаление по id (штрихкод/уникальный ключ)
    @Query("DELETE FROM favorites_table WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)


    // --- РАБОТА С НЕДАВНИМИ (RECENT) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(product: RecentProduct)

    @Query("SELECT * FROM recent_products ORDER BY timestamp DESC LIMIT 20")
    fun getAllRecent(): Flow<List<RecentProduct>>

    @Query("DELETE FROM recent_products")
    suspend fun clearRecent()
}