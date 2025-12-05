package com.imeanttobe.consensusapp.data.remote.interceptor

import com.imeanttobe.consensusapp.core.Constants
import com.imeanttobe.consensusapp.data.repo.IdRepo
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val idRepo: IdRepo
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Intercept current request
        val request = chain.request()
        lateinit var newRequest: Request

        // Check if the request contains our custom "No-Authentication" header
        val isPublicApi = request.header(Constants.NO_AUTH_HEADER) != null
        if (isPublicApi) {
            // 1. If it's public, remove the marker header so the server doesn't see it
            // 2. Do NOT add X-User-Id
            newRequest = request.newBuilder()
                .removeHeader(Constants.NO_AUTH_HEADER)
                .build()
        } else {
            // 1. Get the current User ID
            val userId = idRepo.getCachedIdOrEmpty()

            // 2. Attach the User ID to the request
            newRequest = request.newBuilder()
                .header("X-User-Id", userId)
                .build()
        }

        // Proceed
        return chain.proceed(newRequest)
    }
}