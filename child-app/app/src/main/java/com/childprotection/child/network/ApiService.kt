package com.childprotection.child.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for backend communication.
 */
interface ApiService {

    // ===== Auth & Join =====
    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<ApiResponse<LoginData>>

    @POST("families/join")
    suspend fun joinFamily(@Body body: Map<String, String>): Response<ApiResponse<JoinData>>

    // ===== Consent =====
    @POST("consents/grant")
    suspend fun grantConsent(@Body body: Map<String, String>): Response<ApiResponse<JsonObject>>

    @POST("consents/revoke")
    suspend fun revokeConsent(@Body body: Map<String, String>): Response<ApiResponse<JsonObject>>

    @GET("consents")
    suspend fun getConsents(@Query("childId") childId: String): Response<ApiResponse<List<ConsentData>>>

    // ===== Device =====
    @POST("devices/register")
    suspend fun registerDevice(@Body body: Map<String, String>): Response<ApiResponse<DeviceData>>

    @POST("devices/{id}/heartbeat")
    suspend fun sendHeartbeat(
        @Path("id") deviceId: String,
        @Body body: Map<String, Int>
    ): Response<ApiResponse<Any>>

    // ===== Events =====
    @POST("events/usage")
    suspend fun reportUsageEvents(@Body body: Map<String, Any>): Response<ApiResponse<Any>>

    @POST("events/location")
    suspend fun reportLocation(@Body body: Map<String, Any>): Response<ApiResponse<Any>>

    // ===== Alerts =====
    @POST("alerts")
    suspend fun createAlert(@Body body: Map<String, String>): Response<ApiResponse<JsonObject>>

    // ===== Policies =====
    @GET("policies")
    suspend fun getPolicies(@Query("childId") childId: String): Response<ApiResponse<List<PolicyData>>>

    // ===== Phase 2: Risk Events =====
    @POST("risk/events")
    suspend fun reportRiskEvent(@Body body: Map<String, Any?>): Response<ApiResponse<JsonObject>>

    // Fetch risk events for child (optional date range)
    @GET("risk/events/{childId}")
    suspend fun getRiskEvents(
        @Path("childId") childId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<ApiResponse<List<RiskEventData>>>

    // ===== Phase 2: Web Safety =====
    @POST("web-safety/check")
    suspend fun checkDomain(@Body body: Map<String, String>): Response<ApiResponse<JsonObject>>
    // ===== Phase 3: Live Support Sessions =====
    @POST("support/sessions")
    suspend fun createSupportSession(@Body body: Map<String, String>): Response<ApiResponse<SupportSessionData>>

    @PUT("support/sessions/{sessionId}/end")
    suspend fun endSupportSession(@Path("sessionId") sessionId: String): Response<ApiResponse<SupportSessionData>>

    @GET("support/sessions/{sessionId}")
    suspend fun getSupportSession(@Path("sessionId") sessionId: String): Response<ApiResponse<SupportSessionData>>

    @POST("support/sessions/{sessionId}/log")
    suspend fun addSupportLog(@Path("sessionId") sessionId: String, @Body body: Map<String, String>): Response<ApiResponse<SupportSessionData>>
}

// ===== Response Models =====
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val user: UserData
)

data class UserData(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String
)

data class JoinData(
    val familyId: String,
    val familyName: String,
    val childId: String,
    val childName: String
)

data class ConsentData(
    val id: String,
    val featureName: String,
    val status: String,
    val displayText: String?,
    val policyVersion: String?
)

data class DeviceData(
    val id: String,
    val deviceName: String,
    val deviceModel: String,
    val status: String
)

data class PolicyData(
    val id: String,
    val policyType: String,
    val dailyLimitMinutes: Int?,
    val startTime: String?,
    val endTime: String?,
    val active: Boolean
)

data class RiskEventData(
    val id: String,
    val riskCategory: String,
    val riskLevel: String,
    val confidence: Double,
    val title: String,
    val description: String?,
    val source: String,
    val relatedAppPackage: String?,
    val reviewed: Boolean,
    val createdAt: String
)

data class SupportSessionData(
    val id: String,
    val type: String,
    val status: String,
    val childId: String,
    val initiatorId: String,
    val startedAt: String,
    val endedAt: String?,
    val maxDurationMinutes: Int,
    val logs: String?
)

