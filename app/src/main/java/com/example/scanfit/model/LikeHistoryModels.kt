package com.example.scanfit.model

import com.google.gson.annotations.SerializedName

data class CreateLikeRequest(
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_data") val productData: String,
    val source: String = "openfoodfacts"
)

data class LikeItem(
    val id: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("product_id") val productId: Long,
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_data") val productData: String,
    val source: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class LikeResponse(
    val success: Boolean?,
    val message: String?,
    val data: LikeItem?
)

data class LikeListResponse(
    val success: Boolean?,
    val message: String?,
    val data: List<LikeItem>?
)

data class CreateHistoryRequest(
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_data") val productData: String,
    val source: String = "openfoodfacts"
)

data class HistoryItem(
    val id: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("product_id") val productId: Long,
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_data") val productData: String,
    val source: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class HistoryListResponse(
    val success: Boolean?,
    val message: String?,
    val data: List<HistoryItem>?
)
