package com.childprotection.parent.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface ParentApiService {

    // Auth
    @POST("auth/signup")
    suspend fun signup(@Body body: Map<String, String>): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<ApiResponse<AuthData>>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: Map<String, String>): Response<ApiResponse<AuthData>>

    // Family
    @POST("families")
    suspend fun createFamily(@Body body: Map<String, String>): Response<ApiResponse<FamilyData>>

    @GET("families")
    suspend fun getMyFamilies(): Response<ApiResponse<List<FamilyData>>>

    @GET("families/{id}")
    suspend fun getFamily(@Path("id") id: String): Response<ApiResponse<FamilyData>>

    @GET("families/{id}/members")
    suspend fun getFamilyMembers(@Path("id") id: String): Response<ApiResponse<List<MemberData>>>

    // Consent
    @GET("consents")
    suspend fun getConsents(@Query("childId") childId: String): Response<ApiResponse<List<ConsentData>>>

    @POST("consents/grant")
    suspend fun grantConsent(@Body body: Map<String, String>): Response<ApiResponse<JsonObject>>

    @POST("consents/revoke")
    suspend fun revokeConsent(@Body body: Map<String, String>): Response<ApiResponse<JsonObject>>

    // Children activity
    @GET("children/{id}/screen-time")
    suspend fun getScreenTime(
        @Path("id") childId: String,
        @Query("date") date: String? = null
    ): Response<ApiResponse<JsonObject>>

    @GET("children/{id}/timeline")
    suspend fun getTimeline(
        @Path("id") childId: String,
        @Query("date") date: String? = null
    ): Response<ApiResponse<JsonObject>>

    // Policies
    @POST("policies")
    suspend fun createPolicy(@Body body: Map<String, Any?>): Response<ApiResponse<PolicyData>>

    @GET("policies")
    suspend fun getPolicies(@Query("childId") childId: String): Response<ApiResponse<List<PolicyData>>>

    @DELETE("policies/{id}")
    suspend fun deletePolicy(@Path("id") id: String): Response<ApiResponse<Any>>

    // Alerts
    @GET("alerts")
    suspend fun getAlerts(
        @Query("familyId") familyId: String,
        @Query("unacknowledgedOnly") unacknowledgedOnly: Boolean = false
    ): Response<ApiResponse<List<AlertData>>>

    @PUT("alerts/{id}/acknowledge")
    suspend fun acknowledgeAlert(@Path("id") id: String): Response<ApiResponse<JsonObject>>
}

// ===== Response Models =====
data class ApiResponse<T>(val success: Boolean, val message: String, val data: T?)

data class AuthData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val user: UserData
)

data class UserData(val id: String, val email: String, val displayName: String, val role: String)

data class FamilyData(val id: String, val name: String, val inviteCode: String)

data class MemberData(
    val id: String,
    val displayName: String,
    val role: String,
    val joinedAt: String?
)

data class ConsentData(
    val id: String,
    val featureName: String,
    val status: String,
    val displayText: String?,
    val childId: String,
    val policyVersion: String?,
    val grantedAt: String?,
    val revokedAt: String?
)

data class PolicyData(
    val id: String,
    val policyType: String,
    val dailyLimitMinutes: Int?,
    val startTime: String?,
    val endTime: String?,
    val active: Boolean,
    val childId: String?
)

data class AlertData(
    val id: String,
    val alertType: String,
    val severity: String,
    val title: String,
    val message: String?,
    val acknowledged: Boolean,
    val createdAt: String?,
    val childId: String?
)
