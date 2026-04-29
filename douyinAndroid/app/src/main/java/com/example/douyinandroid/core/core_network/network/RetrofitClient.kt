package com.example.douyinandroid.core.core_network.network

import android.content.Context
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

    private lateinit var authPreferences: AuthPreferences

    fun init(context: Context) {
        authPreferences = AuthPreferences.getInstance(context)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
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
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
