package com.example.scanfit.model

import com.google.gson.annotations.SerializedName

data class UserMeasureRequest(
    val age: String,
    @SerializedName("birth_date")
    val birthDate: String,
    @SerializedName("blood_pressure")
    val bloodPressure: Int,
    val bmi: Int,
    val cholesterol: Int,
    @SerializedName("daily_calories_goal")
    val dailyCaloriesGoal: Int,
    @SerializedName("daily_water_goal")
    val dailyWaterGoal: Int,
    val gender: String,
    val height: Int,
    @SerializedName("user_id")
    val userId: Int,
    val weight: Int
)

data class UpdateUserMeasureRequest(
    val age: String,
    @SerializedName("birth_date")
    val birthDate: String,
    @SerializedName("blood_pressure")
    val bloodPressure: Int,
    val bmi: Int,
    val cholesterol: Int,
    @SerializedName("daily_calories_goal")
    val dailyCaloriesGoal: Int,
    @SerializedName("daily_water_goal")
    val dailyWaterGoal: Int,
    val gender: String,
    val height: Int,
    val weight: Int
)

data class UserMeasureResponse(
    val data: UserMeasureData?,
    val message: String?,
    val success: Boolean
)

data class UserMeasureData(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val age: String?,
    @SerializedName("birth_date")
    val birthDate: String?,
    @SerializedName("blood_pressure")
    val bloodPressure: Int?,
    val bmi: Int?,
    val cholesterol: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("daily_calories_goal")
    val dailyCaloriesGoal: Int?,
    @SerializedName("daily_water_goal")
    val dailyWaterGoal: Int?,
    val gender: String?,
    val height: Int?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    val weight: Int?
)
