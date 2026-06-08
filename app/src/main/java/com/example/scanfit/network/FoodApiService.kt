package com.example.scanfit.network


import com.example.scanfit.model.Product
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

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
        @Part("health_info") healthInfo: RequestBody,
        @Part("user_information") userInformation: RequestBody? = null
    ): AnalysisResponse

    @Multipart
    @POST("analyze-dish")
    suspend fun analyzeDish(
        @Part file: MultipartBody.Part,
        @Part("health_info") healthInfo: RequestBody,
        @Part("user_information") userInformation: RequestBody? = null
    ): AnalysisResponse

    @POST("ingredient")
    suspend fun analyzeIngredients(
        @retrofit2.http.Body data: Map<String, @JvmSuppressWildcards Any>
    ): AnalysisResponse

    @POST("compare-products")
    suspend fun compareProducts(
        @retrofit2.http.Body data: Map<String, @JvmSuppressWildcards Any>
    ): CompareProductsResponse

    @Multipart
    @POST("compare-products-images")
    suspend fun compareProductsImages(
        @Part imageA: MultipartBody.Part,
        @Part imageB: MultipartBody.Part,
        @Part("user_information") userInformation: RequestBody? = null
    ): CompareProductsResponse

}

data class FoodResponse(
    val products: List<Product>
)

data class KaspiSearchResponse(
    val products: List<KaspiProductItem>? = emptyList()
)

data class AnalysisResponse(
    val product_name: String? = null,
    @SerializedName("dish_name")
    val dishName: String? = null,
    val health_score: Int? = 0,
    val risk_level: String? = null,
    val risks: List<AnalysisRisk>? = emptyList(),
    val diet_conflicts: List<AnalysisDietConflict>? = emptyList(),
    val sources: List<AnalysisSource>? = emptyList(),
    val is_food: Boolean? = true,
    val product_type: String? = "unknown",
    val verdict: String? = "No data",
    val macros: AnalysisMacros? = null,
    @SerializedName("estimated_serving")
    val estimatedServing: AnalysisEstimatedServing? = null,
    @SerializedName("estimation_confidence")
    val estimationConfidence: String? = null,
    @SerializedName("identified_ingredients")
    val identifiedIngredients: List<AnalysisIdentifiedIngredient>? = emptyList(),
    val compounds: AnalysisCompounds? = null,
    val alternatives: List<AnalysisAlternative>? = emptyList(),
    @SerializedName("scan_image")
    val scanImage: AnalysisScanImage? = null,
    @SerializedName("product_photo")
    val productPhoto: AnalysisProductPhoto? = null,
    @SerializedName("scan_image_url")
    val scanImageUrl: String? = null,
    @SerializedName("image_path")
    val imagePath: String? = null,
    @SerializedName("daily_impact")
    val dailyImpact: AnalysisDailyImpact? = null,
    @SerializedName("user_context_used")
    val userContextUsed: AnalysisUserContextUsed? = null,
    @SerializedName("kaspi_products")
    val kaspiProducts: List<KaspiProductItem>? = emptyList()
) : java.io.Serializable

data class KaspiProductItem(
    val id: String? = null,
    val title: String? = null,
    val brand: String? = null,
    @SerializedName("shop_link")
    val shopLink: String? = null,
    @SerializedName("kaspi_url")
    val kaspiUrl: String? = null,
    @SerializedName("price_formatted")
    val priceFormatted: String? = null,
    @SerializedName("unit_sale_price")
    val unitSalePrice: Double? = null,
    @SerializedName("preview_image")
    val previewImage: String? = null,
    val rating: Double? = null,
    @SerializedName("reviews_quantity")
    val reviewsQuantity: Int? = null,
    val discount: Int? = null,
    @SerializedName("delivery_duration")
    val deliveryDuration: String? = null
) : java.io.Serializable

data class AnalysisEstimatedServing(
    val amount: Double? = null,
    val unit: String? = null,
    val description: String? = null
) : java.io.Serializable

data class AnalysisIdentifiedIngredient(
    val name: String? = null,
    @SerializedName("estimated_amount")
    val estimatedAmount: String? = null,
    val confidence: String? = null
) : java.io.Serializable

data class AnalysisCompounds(
    val water: Double? = null,
    @SerializedName("saturated_fat")
    val saturatedFat: Double? = null,
    @SerializedName("unsaturated_fat")
    val unsaturatedFat: Double? = null,
    @SerializedName("added_sugar")
    val addedSugar: Double? = null,
    @SerializedName("natural_sugar")
    val naturalSugar: Double? = null,
    val starch: Double? = null,
    val potassium: Double? = null,
    val calcium: Double? = null,
    val iron: Double? = null,
    val magnesium: Double? = null,
    val caffeine: Double? = null
) : java.io.Serializable

data class AnalysisMacros(
    val calories: Double? = 0.0,
    val proteins: Double? = 0.0,
    val carbs: Double? = 0.0,
    val fats: Double? = 0.0,
    val fat: Double? = 0.0,
    val sugar: Double? = 0.0,
    val fiber: Double? = 0.0,
    val sodium: Double? = 0.0,
    val cholesterol: Double? = 0.0,
    @SerializedName("vitamin_a")
    val vitaminA: Double? = 0.0,
    @SerializedName("vitamin_b12")
    val vitaminB12: Double? = 0.0,
    @SerializedName("vitamin_b6")
    val vitaminB6: Double? = 0.0,
    @SerializedName("vitamin_b9")
    val vitaminB9: Double? = 0.0,
    @SerializedName("vitamin_c")
    val vitaminC: Double? = 0.0,
    @SerializedName("vitamin_d")
    val vitaminD: Double? = 0.0,
    @SerializedName("vitamin_e")
    val vitaminE: Double? = 0.0
) : java.io.Serializable

@JsonAdapter(AnalysisRiskAdapter::class)
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

data class AnalysisAlternative(
    val name: String? = null,
    val reason: String? = null,
    @SerializedName("kaspi_link")
    val kaspiLink: String? = null
) : java.io.Serializable

data class AnalysisScanImage(
    val bucket: String? = null,
    val key: String? = null,
    @SerializedName("content_type")
    val contentType: String? = null,
    val url: String? = null
) : java.io.Serializable

data class AnalysisProductPhoto(
    val name: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val barcode: String? = null,
    val brand: String? = null,
    val source: String? = null
) : java.io.Serializable

data class AnalysisDailyImpact(
    val calories: AnalysisDailyImpactItem? = null,
    val carbs: AnalysisDailyImpactItem? = null,
    val fat: AnalysisDailyImpactItem? = null,
    val fiber: AnalysisDailyImpactItem? = null,
    val proteins: AnalysisDailyImpactItem? = null,
    val sodium: AnalysisDailyImpactItem? = null,
    val sugar: AnalysisDailyImpactItem? = null,
    @SerializedName("vitamin_a")
    val vitaminA: AnalysisDailyImpactItem? = null,
    @SerializedName("vitamin_b12")
    val vitaminB12: AnalysisDailyImpactItem? = null,
    @SerializedName("vitamin_b6")
    val vitaminB6: AnalysisDailyImpactItem? = null,
    @SerializedName("vitamin_b9")
    val vitaminB9: AnalysisDailyImpactItem? = null,
    @SerializedName("vitamin_c")
    val vitaminC: AnalysisDailyImpactItem? = null,
    @SerializedName("vitamin_d")
    val vitaminD: AnalysisDailyImpactItem? = null,
    @SerializedName("vitamin_e")
    val vitaminE: AnalysisDailyImpactItem? = null,
    val water: AnalysisDailyImpactItem? = null
) : java.io.Serializable

data class AnalysisDailyImpactItem(
    @SerializedName("amount_in_product")
    val amountInProduct: Double? = null,
    @SerializedName("consumed_today")
    val consumedToday: Double? = null,
    @SerializedName("goal_today")
    val goalToday: Double? = null,
    @SerializedName("after_this_product")
    val afterThisProduct: Double? = null,
    @SerializedName("remaining_to_goal")
    val remainingToGoal: Double? = null,
    val status: String? = null,
    val message: String? = null,
    val unit: String? = null
) : java.io.Serializable

data class AnalysisUserContextUsed(
    @SerializedName("has_user_information")
    val hasUserInformation: Boolean? = null,
    @SerializedName("has_legacy_health_info")
    val hasLegacyHealthInfo: Boolean? = null
) : java.io.Serializable

data class CompareProductsResponse(
    val winner: String? = null,
    @SerializedName("winner_name")
    val winnerName: String? = null,
    @SerializedName("name_a")
    val nameA: String? = null,
    @SerializedName("name_b")
    val nameB: String? = null,
    @SerializedName("health_score_a")
    val healthScoreA: Int? = null,
    @SerializedName("health_score_b")
    val healthScoreB: Int? = null,
    @SerializedName("verdict_a")
    val verdictA: String? = null,
    @SerializedName("verdict_b")
    val verdictB: String? = null,
    val recommendation: String? = null,
    @SerializedName("nutrient_comparison")
    val nutrientComparison: List<NutrientComparison>? = null,
    @SerializedName("risks_a")
    val risksA: List<AnalysisRisk>? = null,
    @SerializedName("risks_b")
    val risksB: List<AnalysisRisk>? = null
) : java.io.Serializable

data class NutrientComparison(
    val nutrient: String? = null,
    @SerializedName("value_a")
    val valueA: String? = null,
    @SerializedName("value_b")
    val valueB: String? = null,
    val better: String? = null,
    val note: String? = null
) : java.io.Serializable

class AnalysisRiskAdapter : JsonDeserializer<AnalysisRisk> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: java.lang.reflect.Type,
        context: JsonDeserializationContext
    ): AnalysisRisk {
        return when {
            json.isJsonNull -> AnalysisRisk()
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                AnalysisRisk(reason = json.asString)
            }
            json.isJsonObject -> {
                val obj = json.asJsonObject
                AnalysisRisk(
                    ingredient = obj.get("ingredient")?.takeIf { !it.isJsonNull }?.asString,
                    reason = obj.get("reason")?.takeIf { !it.isJsonNull }?.asString
                        ?: obj.get("message")?.takeIf { !it.isJsonNull }?.asString
                        ?: obj.get("description")?.takeIf { !it.isJsonNull }?.asString,
                    severity = obj.get("severity")?.takeIf { !it.isJsonNull }?.asString,
                    source_indexes = obj.get("source_indexes")
                        ?.takeIf { it.isJsonArray }
                        ?.asJsonArray
                        ?.mapNotNull { element ->
                            runCatching { element.asInt }.getOrNull()
                        }
                )
            }
            else -> throw JsonParseException("Unsupported risk format: $json")
        }
    }
}
