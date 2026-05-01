package com.example.chefmateapp.data.model.auth

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val success: Boolean,
    val user: User? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    val message: String? = null
)

data class User(
    val id: String? = null,
    val email: String,
    val username: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)