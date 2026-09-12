package com.example.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    val user: SupabaseUser
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String
)

interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/v1/logout")
    suspend fun signOut(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String
    ): Response<Unit>
}
