package com.dowers.unibooks.data.remote
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val success: Boolean,
    val message: String?
)

interface AuthApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}