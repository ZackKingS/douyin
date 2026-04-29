package com.example.douyinandroid.core.core_network.network

import android.content.Context
import com.example.douyinandroid.BuildConfig
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_auth.AuthPreferences
import com.example.douyinandroid.core.core_network.network.interceptor.AuthInterceptor
import com.example.douyinandroid.core.core_network.network.interceptor.ErrorInterceptor
import com.example.douyinandroid.core.core_network.network.interceptor.LogInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TAG = "RetrofitClient"

    private lateinit var authPreferences: AuthPreferences

    fun init(context: Context) {
        authPreferences = AuthPreferences.getInstance(context)
        LogUtil.d(TAG, "RetrofitClient initialized: baseUrl=${ApiConstants.BASE_URL}, timeout=${ApiConstants.TIMEOUT}ms")
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        LogUtil.d(TAG, "Creating OkHttpClient with auth, error and logging interceptors")
        OkHttpClient.Builder()
            .connectTimeout(ApiConstants.TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(ApiConstants.TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(ApiConstants.TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(AuthInterceptor(authPreferences))
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        LogUtil.d(TAG, "Creating Retrofit instance")
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        LogUtil.d(TAG, "Creating ApiService")
        retrofit.create(ApiService::class.java)
    }
}
