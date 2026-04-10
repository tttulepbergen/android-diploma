package com.example.scanfit.network

import com.example.scanfit.model.DietTypeListResponse
import com.example.scanfit.model.DiseaseLevelListResponse
import com.example.scanfit.model.DiseaseListResponse
import com.example.scanfit.model.CreateUserDailyEatRequest
import com.example.scanfit.model.CreateUserDailyEatResponse
import com.example.scanfit.model.UpdateDietTypeRequest
import com.example.scanfit.model.UpdateDietTypeResponse
import com.example.scanfit.model.UpdateDiseaseRequest
import com.example.scanfit.model.UpdateUserCaloriesRequest
import com.example.scanfit.model.UpdateUserMeasureRequest
import com.example.scanfit.model.UpdateWeightManagementRequest
import com.example.scanfit.model.UserAccountResponse
import com.example.scanfit.model.UserCaloriesResponse
import com.example.scanfit.model.UserDetailsResponse
import com.example.scanfit.model.UserMeasureRequest
import com.example.scanfit.model.UserMeasureResponse
import com.example.scanfit.model.UserRoleResponse
import com.example.scanfit.model.WeightManagementResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {

    @GET("api/v1/user/me/role")
    suspend fun getUserRole(
        @Header("Authorization") token: String
    ): UserRoleResponse

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

    @GET("api/v1/user/me")
    suspend fun getUserAccount(
        @Header("Authorization") token: String
    ): UserAccountResponse

    @GET("api/v1/user/me/details")
    suspend fun getUserDetails(
        @Header("Authorization") token: String
    ): UserDetailsResponse

    @GET("api/v1/user/diet-type/list")
    suspend fun getDietTypes(
        @Header("Authorization") token: String
    ): DietTypeListResponse

    @GET("api/v1/user/dietary-preference/list")
    suspend fun getDietaryPreferences(
        @Header("Authorization") token: String
    ): DietTypeListResponse

    @GET("api/v1/user/health-condition/list")
    suspend fun getHealthConditions(
        @Header("Authorization") token: String
    ): DietTypeListResponse

    @GET("api/v1/user/disease/list")
    suspend fun getDiseases(
        @Header("Authorization") token: String
    ): DiseaseListResponse

    @GET("api/v1/user/disease-level/list")
    suspend fun getDiseaseLevels(
        @Header("Authorization") token: String
    ): DiseaseLevelListResponse

    @GET("api/v1/user/weight-management/get")
    suspend fun getWeightManagement(
        @Header("Authorization") token: String
    ): WeightManagementResponse

    @GET("api/v1/user/user-calories/today")
    suspend fun getTodayUserCalories(
        @Header("Authorization") token: String
    ): UserCaloriesResponse

    @GET("api/v1/user/user-calories")
    suspend fun getUserCaloriesByDay(
        @Header("Authorization") token: String,
        @Query("day") day: String
    ): UserCaloriesResponse

    @PUT("api/v1/user/user-calories/update")
    suspend fun updateUserCalories(
        @Header("Authorization") token: String,
        @Query("day") day: String,
        @Body request: UpdateUserCaloriesRequest
    ): UserCaloriesResponse

    @PUT("api/v1/user/user-calories/today/refresh")
    suspend fun refreshTodayUserCalories(
        @Header("Authorization") token: String
    ): UserCaloriesResponse

    @POST("api/v1/product/user-daily-eat/create")
    suspend fun createUserDailyEat(
        @Header("Authorization") token: String,
        @Body request: CreateUserDailyEatRequest
    ): CreateUserDailyEatResponse

    @PUT("api/v1/user/diet-type/update/{id}")
    suspend fun updateDietType(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateDietTypeRequest
    ): UpdateDietTypeResponse

    @PUT("api/v1/user/dietary-preference/update/{id}")
    suspend fun updateDietaryPreference(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateDietTypeRequest
    ): UpdateDietTypeResponse

    @PUT("api/v1/user/health-condition/update/{id}")
    suspend fun updateHealthCondition(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateDietTypeRequest
    ): UpdateDietTypeResponse

    @PUT("api/v1/user/disease/update/{id}")
    suspend fun updateDisease(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateDiseaseRequest
    ): UpdateDietTypeResponse

    @PUT("api/v1/user/weight-management/update")
    suspend fun updateWeightManagement(
        @Header("Authorization") token: String,
        @Body request: UpdateWeightManagementRequest
    ): UpdateDietTypeResponse
}
