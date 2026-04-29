package com.example.douyinandroid.core.core_network.network.interceptor

import com.example.douyinandroid.common.common_utils.LogUtil
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

class LogInterceptor : Interceptor {

    companion object {
        private const val TAG = "LogInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestId = UUID.randomUUID().toString().take(8)

        LogUtil.d(TAG, "[$requestId] --> ${request.method} ${request.url}")
        LogUtil.d(TAG, "[$requestId] Headers: ${request.headers.toSafeLogString()}")
        request.body?.let { body ->
            LogUtil.d(TAG, "[$requestId] Request body: contentType=${body.contentType()}, contentLength=${body.contentLength()}")
        } ?: LogUtil.d(TAG, "[$requestId] Request body: empty")

        val startTime = System.currentTimeMillis()
        return try {
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            LogUtil.d(TAG, "[$requestId] <-- ${response.code} ${response.message} (${duration}ms)")
            LogUtil.d(TAG, "[$requestId] <-- ${response.request.url}")
            LogUtil.d(TAG, "[$requestId] Response headers: ${response.headers.toSafeLogString()}")

            response
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            LogUtil.e(TAG, "[$requestId] Network call failed after ${duration}ms: ${request.method} ${request.url}", e)
            throw e
        }
    }

    private fun Headers.toSafeLogString(): String {
        if (size == 0) return "empty"

        return names().joinToString(separator = "; ") { name ->
            val value = if (name.equals("Authorization", ignoreCase = true)) {
                "<redacted>"
            } else {
                values(name).joinToString()
            }
            "$name=$value"
        }
    }
}
