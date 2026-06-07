package com.example.scanfit.network

data class RegisterDeviceTokenRequest(
    val token: String,
    val platform: String = "android"
)
