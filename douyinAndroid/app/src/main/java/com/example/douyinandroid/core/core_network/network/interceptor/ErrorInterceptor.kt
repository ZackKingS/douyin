package com.example.douyinandroid.core.core_network.network.interceptor

import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_network.network.exception.ApiException
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

private const val TAG = "ErrorInterceptor"

class ErrorInterceptor : Interceptor {

    companion object {
        private const val CODE_SUCCESS = 200
        private const val CODE_UNAUTHORIZED = 401
        private const val CODE_FORBIDDEN = 403
        private const val CODE_NOT_FOUND = 404
        private const val CODE_TOO_MANY_REQUESTS = 429
        private const val CODE_SERVER_ERROR = 500
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        LogUtil.d(TAG, "Intercepting request: ${request.url}")
        var response: Response? = null

        try {
            response = chain.proceed(request)
            LogUtil.d(TAG, "Response received: code=${response.code}, isSuccessful=${response.isSuccessful}")

            if (!response.isSuccessful) {
                val code = response.code
                val message = response.message
                val errorBody = response.peekBody(Long.MAX_VALUE)?.string()

                LogUtil.e(TAG, "HTTP Error: $code $message")
                LogUtil.e(TAG, "Error body: $errorBody")

                val errorMessage = parseErrorMessage(errorBody) ?: message
                throw ApiException(code, errorMessage)
            }

            // 检查响应体中的业务状态码
            val body = response.peekBody(Long.MAX_VALUE)?.string()
            LogUtil.d(TAG, "Response body for business code check: $body")
            body?.let {
                val jsonObject = try {
                    JSONObject(it)
                } catch (e: Exception) {
                    LogUtil.e(TAG, "Failed to parse response body as JSON: ${e.message}")
                    null
                }

                jsonObject?.let { json ->
                    val code = json.optInt("code", CODE_SUCCESS)
                    LogUtil.d(TAG, "Business code from response: $code")
                    if (code != CODE_SUCCESS) {
                        val message = json.optString("message", "Unknown error")
                        LogUtil.e(TAG, "Business Error: $code $message")
                        throw ApiException(code, message)
                    }
                }
            }

            return response
        } catch (e: ApiException) {
            LogUtil.e(TAG, "API Exception: ${e.code} ${e.message}")
            handleApiException(e)
            throw e
        } catch (e: Exception) {
            LogUtil.e(TAG, "Network Exception", e)
            throw ApiException(-1, "Network error: ${e.message}")
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrEmpty()) return null

        return try {
            val jsonObject = JSONObject(errorBody)
            jsonObject.optString("message")
        } catch (e: Exception) {
            null
        }
    }

    private fun handleApiException(exception: ApiException) {
        when (exception.code) {
            CODE_UNAUTHORIZED -> {
                // Token 过期，需要重新登录
                LogUtil.w(TAG, "Token expired, need to re-login")
            }
            CODE_FORBIDDEN -> {
                // 权限不足
                LogUtil.w(TAG, "Permission denied")
            }
            CODE_NOT_FOUND -> {
                // 资源不存在
                LogUtil.w(TAG, "Resource not found")
            }
            CODE_TOO_MANY_REQUESTS -> {
                // 请求过于频繁
                LogUtil.w(TAG, "Too many requests")
            }
            CODE_SERVER_ERROR -> {
                // 服务器错误
                LogUtil.e(TAG, "Server error")
            }
        }
    }
}
