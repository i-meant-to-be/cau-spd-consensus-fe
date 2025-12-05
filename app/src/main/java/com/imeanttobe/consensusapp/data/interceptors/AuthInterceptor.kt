package com.imeanttobe.consensusapp.data.interceptors

import com.imeanttobe.consensusapp.core.Constants
import com.imeanttobe.consensusapp.data.repo.IdRepo
import kotlinx.coroutines.runBlocking
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
            // 1. Get the current User ID using runBlocking
            // This blocks the network thread until the ID is retrieved from disk
            val userIdRequest = runBlocking {
                idRepo.getId()
            }
            lateinit var userId: String

            // 2. Check whether the request for id is successful
            if (userIdRequest.isSuccess) {
                val tempUserId = userIdRequest.getOrNull()
                userId = tempUserId ?: ""
            } else {
                userId = ""
            }

            // 3. Create the new request with the header
            newRequest = request.newBuilder()
                .header("X-User-Id", userId ?: "")
                .build()
        }

        // Proceed
        return chain.proceed(newRequest)
    }
}