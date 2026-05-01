package com.example.chefmateapp.data.remote

import com.example.chefmateapp.data.model.auth.*
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    @FormUrlEncoded
    @POST("ChefMate/users/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String,
        @Field("captcha_token") captchaToken: String
    ): Response<AuthResponse>

    @POST("ChefMate/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("ChefMate/users/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<AuthResponse>

    @GET("ChefMate/auth/me")
    suspend fun getCurrentUser(): Response<User>

    @PATCH("ChefMate/users/logout")
    suspend fun logout(): Response<AuthResponse>
}