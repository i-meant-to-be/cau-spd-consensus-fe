package com.imeanttobe.consensusapp.data.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class LoggingInterceptor : Interceptor {
    companion object {
        private const val TAG = "API_LOG"
        private const val MAX_BYTES_TO_READ = 1024L // Read only first 1KB
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 1. Log Request Details
        Log.d(TAG, "--> ${request.method} ${request.url}")

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "<-- HTTP FAILED: $e")
            throw e
        }
        val tookMs = (System.nanoTime() - startNs) / 1e6

        // 2. Log Response Details
        Log.d(TAG, "<-- ${response.code} ${response.message} (${tookMs}ms)")

        // 3. Log a small portion of the body safely
        // 'peekBody' clones the stream so the original remains valid for Retrofit
        val peekedBody = response.peekBody(MAX_BYTES_TO_READ)

        Log.d(TAG, "Body (First $MAX_BYTES_TO_READ bytes): ${peekedBody.string()}")
        Log.d(TAG, "<-- END HTTP")

        return response
    }
}