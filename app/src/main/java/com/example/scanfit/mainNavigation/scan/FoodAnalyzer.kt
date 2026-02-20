package com.example.scanfit.mainNavigation.scan

import android.graphics.Bitmap
import android.util.Log
import com.example.scanfit.network.AnalysisResponse
import com.example.scanfit.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object FoodAnalyzer {


    suspend fun analyzeIngredients(bitmap: Bitmap, healthInfo: String): AnalysisResponse? {
        return withContext(Dispatchers.IO) { // Добавь контекст IO
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                val byteArray = stream.toByteArray()

                val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", "scan.jpg", requestFile)
                val healthInfoBody = healthInfo.toRequestBody("text/plain".toMediaTypeOrNull())

                NetworkClient.aiApiService.analyzeScan(body, healthInfoBody)
            } catch (e: Exception) {
                Log.e("AI_DEBUG", "Error in FoodAnalyzer: ${e.message}")
                null // Возвращаем null при ошибке
            }
        }
    }


    suspend fun analyzeTextIngredientsFull(ingredients: String, healthInfo: String): AnalysisResponse {
        val requestData = mapOf(
            "ingredients" to ingredients,
            "health_info" to healthInfo
        )

        return NetworkClient.aiApiService.analyzeIngredients(requestData)
    }
}