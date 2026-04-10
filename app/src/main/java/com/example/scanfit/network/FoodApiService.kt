package com.example.scanfit.network


import com.example.scanfit.model.Product
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FoodApiService {

    @GET("cgi/search.pl?action=process&json=true")
    suspend fun getProductsByCategory(
        @Query("tagtype_0") tagType: String = "categories",
        @Query("tag_contains_0") tagContains: String = "contains",
        @Query("tag_0") category: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = "product_name,brands,image_url,nutriscore_grade,nutriments,nutriments_estimated,ingredients_text,ingredients_text_en"
    ): FoodResponse

    @GET("cgi/search.pl?action=process&json=true")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("page_size") pageSize: Int = 50,
        @Query("fields") fields: String = "product_name,brands,image_url,nutriscore_grade,nutriments,nutriments_estimated,ingredients_text,ingredients_text_en"
    ): FoodResponse

    @Multipart
    @POST("analyze-scan")
    suspend fun analyzeScan(
        @Part file: MultipartBody.Part,
        @Part("health_info") healthInfo: RequestBody
    ): AnalysisResponse

    @POST("ingredient")
    suspend fun analyzeIngredients(
        @retrofit2.http.Body data: Map<String, @JvmSuppressWildcards Any>
    ): AnalysisResponse
}

data class FoodResponse(
    val products: List<Product>
)

data class AnalysisResponse(
    val product_name: String? = null,
    val health_score: Int? = 0,
    val risk_level: String? = null,
    val risks: List<AnalysisRisk>? = emptyList(),
    val diet_conflicts: List<AnalysisDietConflict>? = emptyList(),
    val sources: List<AnalysisSource>? = emptyList(),
    val is_food: Boolean? = true,
    val product_type: String? = "unknown",
    val verdict: String? = "No data",
    val macros: AnalysisMacros? = null
) : java.io.Serializable

data class AnalysisMacros(
    val calories: Double? = 0.0,
    val proteins: Double? = 0.0,
    val carbs: Double? = 0.0,
    val fats: Double? = 0.0,
    val sugar: Double? = 0.0,
    val fiber: Double? = 0.0,
    val sodium: Double? = 0.0,
    val cholesterol: Double? = 0.0
) : java.io.Serializable

data class AnalysisRisk(
    val ingredient: String? = null,
    val reason: String? = null,
    val severity: String? = null,
    val source_indexes: List<Int>? = emptyList()
) : java.io.Serializable

data class AnalysisDietConflict(
    val diet_code: String? = null,
    val reason: String? = null,
    val severity: String? = null
) : java.io.Serializable

data class AnalysisSource(
    val title: String? = null,
    val url: String? = null,
    val source_type: String? = null
) : java.io.Serializable
