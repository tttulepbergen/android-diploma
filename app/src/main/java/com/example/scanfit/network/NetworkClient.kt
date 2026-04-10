package com.example.scanfit.network

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"
    private const val AI_BASE_URL = "http://46.101.137.109:8001/"
    private const val AUTH_BASE_URL = "http://46.101.137.109:3000/"
    private const val OPEN_FOOD_FACTS_MAX_RETRIES = 10

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val isOpenFoodFactsRequest = request.url.host.contains("openfoodfacts.org")
            if (isOpenFoodFactsRequest) {
                Log.d("OPENFOODFACTS_API", "${request.method} ${request.url}")
            }

            var response = chain.proceed(request)
            var retryCount = 0

            while (isOpenFoodFactsRequest && response.code == 503 && retryCount < OPEN_FOOD_FACTS_MAX_RETRIES) {
                retryCount++
                Log.w("OPENFOODFACTS_API", "503 received, retry $retryCount/$OPEN_FOOD_FACTS_MAX_RETRIES: ${request.url}")
                response.close()
                Thread.sleep((retryCount * 500L).coerceAtMost(3_000L))
                response = chain.proceed(request)
            }

            response
        }
        .connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(3, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()

    val apiService: FoodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoodApiService::class.java)
    }

    val aiApiService: FoodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoodApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApiService::class.java)
    }
}
