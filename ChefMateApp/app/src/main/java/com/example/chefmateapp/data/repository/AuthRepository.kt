package com.example.chefmateapp.data.repository

import com.example.chefmateapp.data.model.auth.AuthResponse
import com.example.chefmateapp.data.model.auth.RegisterRequest
import com.example.chefmateapp.data.model.auth.User
import com.example.chefmateapp.data.remote.AuthService
import retrofit2.Response

class AuthRepository(private val authService: AuthService) {
    suspend fun login(email: String, password: String, captchaToken: String): Response<AuthResponse> {
        return authService.login(email, password, captchaToken)
    }

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return authService.register(request)
    }

    suspend fun getCurrentUser(): Response<User> {
        return authService.getCurrentUser()
    }

    suspend fun logout(): Response<AuthResponse> {
        return authService.logout()
    }
}