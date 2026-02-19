package com.example.scanfit.analyzer

import android.graphics.Bitmap
import com.example.scanfit.network.NetworkClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object FoodAnalyzer {

    // Добавляем параметр healthInfo (строка с болезнями)
    suspend fun analyzeIngredients(bitmap: Bitmap, healthInfo: String): String {
        return try {
            // 1. Конвертируем картинку в байты
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream) // 70 достаточно для ИИ
            val byteArray = stream.toByteArray()

            // 2. Готовим Multipart запрос (файл)
            val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "scan.jpg", requestFile)

            // 3. Готовим вторую часть запроса (текст о здоровье)
            val healthInfoBody = healthInfo.toRequestBody("text/plain".toMediaTypeOrNull())

            // 4. Отправляем в Gateway — теперь передаем ОБА параметра
            val response = NetworkClient.aiApiService.analyzeScan(body, healthInfoBody)

            // Возвращаем вердикт
            response.verdict
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}