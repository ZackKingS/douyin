package com.example.douyinandroid.core.core_network.network.interceptor

import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_auth.AuthPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authPreferences: AuthPreferences
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private const val HEADER_ACCEPT = "Accept"
        private const val HEADER_X_APP_VERSION = "X-App-Version"
        private const val HEADER_X_PLATFORM = "X-Platform"
        private const val HEADER_X_DEVICE_ID = "X-Device-Id"

        private const val TOKEN_PREFIX = "Bearer "
        private const val CONTENT_TYPE_JSON = "application/json"
        private const val ACCEPT_JSON = "application/json"
        private const val PLATFORM = "android"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val requestBuilder = originalRequest.newBuilder()
            .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
            .header(HEADER_ACCEPT, ACCEPT_JSON)
            .header(HEADER_X_PLATFORM, PLATFORM)
            .header(HEADER_X_APP_VERSION, getAppVersion())
            .header(HEADER_X_DEVICE_ID, getDeviceId())

        // 添加 Token
        getToken()?.let { token ->
            requestBuilder.header(HEADER_AUTHORIZATION, TOKEN_PREFIX + token)
            LogUtil.d(TAG, "Token added to request")
        } ?: LogUtil.d(TAG, "No token available")

        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    private fun getToken(): String? {
        return authPreferences.token
    }

    private fun getDeviceId(): String {
        // TODO: 获取设备唯一ID
        return android.os.Build.SERIAL
    }

    private fun getAppVersion(): String {
        return try {
            val context = com.example.douyinandroid.DouyinApp.instance
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}
