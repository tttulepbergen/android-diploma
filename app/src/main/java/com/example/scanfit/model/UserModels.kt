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

data class UserAccountResponse(
    val data: UserAccountData?,
    val message: String?,
    val success: Boolean
)

data class UserAccountData(
    val id: Int,
    val email: String,
    val username: String?,
    @SerializedName("birth_date")
    val birthDate: String?,
    val mygoal: String?,
    val photo: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class DietTypeListResponse(
    val data: List<DietType>?,
    val message: String?,
    val success: Boolean
)

data class DietType(
    val id: Int,
    val name: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    val category: String?
)

data class UpdateDietTypeRequest(
    @SerializedName("is_active")
    val isActive: Boolean
)

data class UpdateDietTypeResponse(
    val message: String?,
    val success: Boolean
)

data class DiseaseListResponse(
    val data: List<Disease>?,
    val message: String?,
    val success: Boolean
)

data class Disease(
    val id: Int,
    val code: String?,
    val name: String,
    val description: String?,
    @SerializedName("disease_level")
    val diseaseLevel: DiseaseLevel?,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class DiseaseLevelListResponse(
    val data: List<DiseaseLevel>?,
    val message: String?,
    val success: Boolean
)

data class DiseaseLevel(
    val id: Int,
    val code: String?,
    val name: String
)

data class UpdateDiseaseRequest(
    @SerializedName("disease_level_id")
    val diseaseLevelId: Int,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class WeightManagementResponse(
    val data: WeightManagementData?,
    val message: String?,
    val success: Boolean
)

data class WeightManagementData(
    val id: Int,
    val goal: String?,
    @SerializedName("target_date")
    val targetDate: String?,
    @SerializedName("target_weight")
    val targetWeight: Int?,
    @SerializedName("weekly_weight_change")
    val weeklyWeightChange: Int?
)

data class UpdateWeightManagementRequest(
    val goal: String?,
    @SerializedName("target_date")
    val targetDate: String?,
    @SerializedName("target_weight")
    val targetWeight: Int?,
    @SerializedName("weekly_weight_change")
    val weeklyWeightChange: Int?
)
