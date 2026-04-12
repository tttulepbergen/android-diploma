package com.example.scanfit.network

import com.example.scanfit.model.BaseResponse
import com.example.scanfit.model.DietTypeListResponse
import com.example.scanfit.model.DiseaseLevelListResponse
import com.example.scanfit.model.DiseaseListResponse
import com.example.scanfit.model.CreateProductScanRequest
import com.example.scanfit.model.CreateProductScanResponse
import com.example.scanfit.model.CreateUserDailyEatRequest
import com.example.scanfit.model.CreateUserDailyEatResponse
import com.example.scanfit.model.ConsumptionHistoryResponse
import com.example.scanfit.model.ProductScanResponse
import com.example.scanfit.model.ProductScanLimitResponse
import com.example.scanfit.model.ProductScanLimitDecreaseResponse
import com.example.scanfit.model.UpdateDietTypeRequest
import com.example.scanfit.model.UpdateDietTypeResponse
import com.example.scanfit.model.UpdateDiseaseRequest
import com.example.scanfit.model.UpdateUserCaloriesRequest
import com.example.scanfit.model.UpdateUserWaterRequest
import com.example.scanfit.model.UpdateUserMeasureRequest
import com.example.scanfit.model.UpdateRegistrationStatusRequest
import com.example.scanfit.model.UpdateWeightManagementRequest
import com.example.scanfit.model.RegistrationStatusResponse
import com.example.scanfit.model.UserAccountResponse
import com.example.scanfit.model.UserCaloriesResponse
import com.example.scanfit.model.UserFirstDayResponse
import com.example.scanfit.model.UserDetailsResponse
import com.example.scanfit.model.UserMeasureRequest
import com.example.scanfit.model.UserMeasureResponse
import com.example.scanfit.model.UserRoleResponse
import com.example.scanfit.model.UserWaterResponse
import com.example.scanfit.model.WeightManagementResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @GET("api/v1/user/registration-status/me")
    suspend fun getRegistrationStatus(
        @Header("Authorization") token: String
    ): RegistrationStatusResponse

    @PUT("api/v1/user/registration-status/me")
    suspend fun updateRegistrationStatus(
        @Header("Authorization") token: String,
        @Body request: UpdateRegistrationStatusRequest
    ): BaseResponse

    @Multipart
    @POST("api/v1/user/change-picture")
    suspend fun changePicture(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): BaseResponse

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

    @GET("api/v1/user/user-calories/first-day")
    suspend fun getUserCaloriesFirstDay(
        @Header("Authorization") token: String
    ): UserFirstDayResponse

    @GET("api/v1/users/me/consumption/history")
    suspend fun getConsumptionHistory(
        @Header("Authorization") token: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): ConsumptionHistoryResponse

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

    @GET("api/v1/user/user-water/today")
    suspend fun getTodayUserWater(
        @Header("Authorization") token: String
    ): UserWaterResponse

    @GET("api/v1/user/user-water")
    suspend fun getUserWaterByDay(
        @Header("Authorization") token: String,
        @Query("day") day: String
    ): UserWaterResponse

    @PUT("api/v1/user/user-water/update")
    suspend fun updateUserWater(
        @Header("Authorization") token: String,
        @Query("day") day: String,
        @Body request: UpdateUserWaterRequest
    ): UserWaterResponse

    @POST("api/v1/product/user-daily-eat/create")
    suspend fun createUserDailyEat(
        @Header("Authorization") token: String,
        @Body request: CreateUserDailyEatRequest
    ): CreateUserDailyEatResponse

    @POST("api/v1/product/product-scans/create")
    suspend fun createProductScan(
        @Header("Authorization") token: String,
        @Body request: CreateProductScanRequest
    ): CreateProductScanResponse

    @PUT("api/v1/product/product-scans/update/{id}")
    suspend fun updateProductScan(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateProductScanRequest
    ): CreateProductScanResponse

    @GET("api/v1/product/product-scans/get-by-product-name")
    suspend fun getProductScanByProductName(
        @Header("Authorization") token: String,
        @Query("product_name") productName: String
    ): ProductScanResponse

    @GET("api/v1/product/product-scans/limit")
    suspend fun getProductScanLimit(
        @Header("Authorization") token: String
    ): ProductScanLimitResponse

    @POST("api/v1/product/product-scans/limit/decrease")
    suspend fun decreaseProductScanLimit(
        @Header("Authorization") token: String
    ): ProductScanLimitDecreaseResponse

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
