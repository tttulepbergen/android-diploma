package com.example.scanfit.network

import com.example.scanfit.model.AuthResponse
import com.example.scanfit.model.BaseResponse
import com.example.scanfit.model.ForgotPasswordRequest
import com.example.scanfit.model.LoginRequest
import com.example.scanfit.model.RegisterRequest
import com.example.scanfit.model.ResetPasswordRequest
import com.example.scanfit.model.VerifyPinRequest
import com.example.scanfit.model.VerifyPinResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/v1/auth/password/forgot")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): BaseResponse

    @POST("api/v1/auth/password/verify-pin")
    suspend fun verifyPin(
        @Body request: VerifyPinRequest
    ): VerifyPinResponse

    @POST("api/v1/auth/password/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): BaseResponse
}
