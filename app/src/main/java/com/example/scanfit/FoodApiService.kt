package com.example.scanfit

import com.example.scanfit.model.Product
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FoodApiService {

    // 1. Поиск по категориям (уже был у тебя)
    @GET("cgi/search.pl?action=process&json=true")
    suspend fun getProductsByCategory(
        @Query("tagtype_0") tagType: String = "categories",
        @Query("tag_contains_0") tagContains: String = "contains",
        @Query("tag_0") category: String,
        @Query("page_size") pageSize: Int = 20
    ): FoodResponse

    // 2. Поиск по ключевым словам (добавляем для SearchFragment)
    @GET("cgi/search.pl?action=process&json=true")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("page_size") pageSize: Int = 50
    ): FoodResponse

    @Multipart
    @POST("analyze-scan")
    suspend fun analyzeScan(
        @Part file: MultipartBody.Part
    ): AnalysisResponse
}

data class FoodResponse(
    val products: List<Product>
)

data class AnalysisResponse(
    val health_score: Int,
    val risks: List<String>,
    val verdict: String
)