package com.example.douyinandroid.core.core_network.network.interceptor

import com.example.douyinandroid.common.common_utils.LogUtil
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
        LogUtil.d(TAG, "[$requestId] Headers: ${request.headers}")

        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        LogUtil.d(TAG, "[$requestId] <-- ${response.code} ${response.message} (${duration}ms)")
        LogUtil.d(TAG, "[$requestId] <-- ${response.request.url}")

        return response
    }
}
