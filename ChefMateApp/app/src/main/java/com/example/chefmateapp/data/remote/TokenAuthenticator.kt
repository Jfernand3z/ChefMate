package com.example.chefmateapp.data.remote

import com.example.chefmateapp.data.model.auth.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {

    companion object {
        private val lock = Any()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("users/refresh")) return null
        if (response.responseCount() >= 2) return null

        val tokenBeforeRefresh = NetworkModule.getAccessToken()

        synchronized(lock) {
            val tokenAfterLock = NetworkModule.getAccessToken()

            if (tokenAfterLock != null && tokenAfterLock != tokenBeforeRefresh) {
                return retryWithToken(response, tokenAfterLock)
            }

            val storedRefreshToken = NetworkModule.getRefreshToken() ?: return null

            val refreshResult = runBlocking {
                try {
                    NetworkModule.refreshAuthService.refreshToken(RefreshRequest(storedRefreshToken))
                } catch (e: Exception) {
                    null
                }
            }

            if (refreshResult == null || !refreshResult.isSuccessful) {
                NetworkModule.clearTokens()
                return null
            }

            val body = refreshResult.body()
            val newAccess = body?.accessToken
            val newRefresh = body?.refreshToken

            if (newAccess == null || newRefresh == null) {
                NetworkModule.clearTokens()
                return null
            }

            NetworkModule.setTokens(newAccess, newRefresh)
            return retryWithToken(response, newAccess)
        }
    }

    private fun retryWithToken(response: Response, token: String): Request =
        response.request.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer $token")
            .build()
}

private fun Response.responseCount(): Int {
    var count = 1
    var prior = priorResponse
    while (prior != null) {
        count++
        prior = prior.priorResponse
    }
    return count
}
