package com.example.scanfit.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyPinRequest(
    val email: String,
    @SerializedName("pin_code")
    val pinCode: String
)

data class VerifyPinResponse(
    val data: VerifyPinData?,
    val message: String?,
    val success: Boolean
)

data class VerifyPinData(
    val token: String
)

data class ResetPasswordRequest(
    val email: String,
    @SerializedName("new_password")
    val newPassword: String,
    @SerializedName("new_password_confirmation")
    val newPasswordConfirmation: String,
    val token: String
)

data class BaseResponse(
    val message: String?,
    val success: Boolean
)

data class AuthResponse(
    val data: AuthData?,
    val message: String?,
    val success: Boolean
)

data class AuthData(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("token_type")
    val tokenType: String
)
