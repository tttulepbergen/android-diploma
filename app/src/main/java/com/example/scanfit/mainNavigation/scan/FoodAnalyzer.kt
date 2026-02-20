package com.example.scanfit.mainNavigation.scan

import android.graphics.Bitmap
import com.example.scanfit.network.NetworkClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object FoodAnalyzer {

    suspend fun analyzeIngredients(bitmap: Bitmap, healthInfo: String): String {
        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            val byteArray = stream.toByteArray()

            val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "scan.jpg", requestFile)

            val healthInfoBody = healthInfo.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = NetworkClient.aiApiService.analyzeScan(body, healthInfoBody)

            // ИСПРАВЛЕНИЕ ТУТ: Добавляем значение по умолчанию
            response.verdict ?: "Вердикт отсутствует"

        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}