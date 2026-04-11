package com.example.scanfit.model

import com.google.gson.JsonElement
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

data class UserRoleResponse(
    val data: UserRole?,
    val message: String?,
    val success: Boolean
)

data class UserDetailsResponse(
    val data: UserDetailsData?,
    val message: String?,
    val success: Boolean
)

data class UserDetailsData(
    @SerializedName("active_diet_types")
    val activeDietTypes: List<DietType>?,
    @SerializedName("active_dietary_preferences")
    val activeDietaryPreferences: List<DietType>?,
    @SerializedName("active_diseases")
    val activeDiseases: List<Disease>?,
    @SerializedName("active_health_conditions")
    val activeHealthConditions: List<DietType>?,
    val measure: UserMeasureData?,
    val user: UserAccountData?,
    @SerializedName("weight_management")
    val weightManagement: WeightManagementData?
)

data class UserAccountData(
    val id: Int,
    val email: String,
    val username: String?,
    @SerializedName("birth_date")
    val birthDate: String?,
    val mygoal: String?,
    val photo: String?,
    val role: UserRole?,
    @SerializedName("role_id")
    val roleId: Int?,
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
    val code: String?,
    val description: String?,
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
    val weeklyWeightChange: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
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

data class UserCaloriesResponse(
    val data: UserCaloriesData?,
    val message: String?,
    val success: Boolean
)

data class UserFirstDayResponse(
    val data: UserFirstDayData?,
    val message: String?,
    val success: Boolean
)

data class UserFirstDayData(
    @SerializedName("first_day")
    val firstDay: String?
)

data class UserWaterResponse(
    val data: UserWaterData?,
    val message: String?,
    val success: Boolean
)

data class UpdateUserWaterRequest(
    val water: Int
)

data class UserWaterData(
    val id: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    val water: Int?,
    val day: String?,
    val daily: UserWaterDaily?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class UserWaterDaily(
    val id: Int?,
    val day: String?,
    val water: Int?,
    val goal: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class CreateUserDailyEatResponse(
    val message: String?,
    val success: Boolean?
)

data class CreateProductScanRequest(
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("scan_information")
    val scanInformation: JsonElement
)

data class CreateProductScanResponse(
    val message: String?,
    val success: Boolean?
)

data class ProductScanResponse(
    val data: JsonElement?,
    val message: String?,
    val success: Boolean
)

data class ProductScanData(
    val id: Int?,
    @SerializedName("product_name")
    val productName: String?,
    @SerializedName("scan_information")
    val scanInformation: JsonElement?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class CreateUserDailyEatRequest(
    val calorie: Int,
    val carbohydrate: Int,
    val cholesterol: Int,
    val fats: Int,
    val fiber: Int,
    val portion: Double,
    @SerializedName("product_name")
    val productName: String,
    val protein: Int,
    val sodium: Int,
    val sugar: Int,
    @SerializedName("vitamin_a")
    val vitaminA: Double,
    @SerializedName("vitamin_b12")
    val vitaminB12: Double,
    @SerializedName("vitamin_b6")
    val vitaminB6: Double,
    @SerializedName("vitamin_b9")
    val vitaminB9: Double,
    @SerializedName("vitamin_c")
    val vitaminC: Double,
    @SerializedName("vitamin_d")
    val vitaminD: Double,
    @SerializedName("vitamin_e")
    val vitaminE: Double
)

data class UpdateUserCaloriesRequest(
    val calories: Int,
    val carbs: Int,
    val fat: Int,
    val proteins: Int,
    val fiber: Int? = null,
    val sodium: Int? = null,
    val sugar: Int? = null,
    val cholesterol: Int? = null,
    @SerializedName("vitamin_a")
    val vitaminA: Double? = null,
    @SerializedName("vitamin_b12")
    val vitaminB12: Double? = null,
    @SerializedName("vitamin_b6")
    val vitaminB6: Double? = null,
    @SerializedName("vitamin_b9")
    val vitaminB9: Double? = null,
    @SerializedName("vitamin_c")
    val vitaminC: Double? = null,
    @SerializedName("vitamin_d")
    val vitaminD: Double? = null,
    @SerializedName("vitamin_e")
    val vitaminE: Double? = null
)

data class UserCaloriesData(
    val id: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    val calories: Int?,
    val carbs: Int?,
    val fat: Int?,
    val fiber: Int?,
    val proteins: Int?,
    val sodium: Int?,
    val sugar: Int?,
    val cholesterol: Int?,
    @SerializedName("vitamin_a")
    val vitaminA: Double?,
    @SerializedName("vitamin_b12")
    val vitaminB12: Double?,
    @SerializedName("vitamin_b6")
    val vitaminB6: Double?,
    @SerializedName("vitamin_b9")
    val vitaminB9: Double?,
    @SerializedName("vitamin_c")
    val vitaminC: Double?,
    @SerializedName("vitamin_d")
    val vitaminD: Double?,
    @SerializedName("vitamin_e")
    val vitaminE: Double?,
    val day: String?,
    val daily: UserCaloriesDaily?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class UserCaloriesDaily(
    val id: Int?,
    val day: String?,
    val calories: Int?,
    val carbs: Int?,
    val fat: Int?,
    val fiber: Int?,
    val proteins: Int?,
    val sodium: Int?,
    val sugar: Int?,
    val cholesterol: Int?,
    @SerializedName("vitamin_a")
    val vitaminA: Double?,
    @SerializedName("vitamin_b12")
    val vitaminB12: Double?,
    @SerializedName("vitamin_b6")
    val vitaminB6: Double?,
    @SerializedName("vitamin_b9")
    val vitaminB9: Double?,
    @SerializedName("vitamin_c")
    val vitaminC: Double?,
    @SerializedName("vitamin_d")
    val vitaminD: Double?,
    @SerializedName("vitamin_e")
    val vitaminE: Double?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)
