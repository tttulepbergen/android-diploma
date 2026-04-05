package com.example.scanfit.network

import com.example.scanfit.model.UpdateUserMeasureRequest
import com.example.scanfit.model.UserMeasureRequest
import com.example.scanfit.model.UserMeasureResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {

    @POST("api/v1/user/measure/create")
    suspend fun createMeasure(
        @Header("Authorization") token: String,
        @Body request: UserMeasureRequest
    ): UserMeasureResponse

    @GET("api/v1/user/measure/get")
    suspend fun getMeasure(
        @Header("Authorization") token: String
    ): UserMeasureResponse

    @PUT("api/v1/user/measure/update")
    suspend fun updateMeasure(
        @Header("Authorization") token: String,
        @Body request: UpdateUserMeasureRequest
    ): UserMeasureResponse
}
