package com.example.scanfit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    // Создаем OkHttpClient с увеличенными таймаутами
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Таймаут подключения: 30 секунд
        .readTimeout(30, TimeUnit.SECONDS)   // Таймаут чтения: 30 секунд
        .writeTimeout(30, TimeUnit.SECONDS)   // Таймаут записи: 30 секунд
        .retryOnConnectionFailure(true)      // Повторять при ошибке подключения
        .build()

    val apiService: FoodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Используем настроенный OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoodApiService::class.java)
    }
}