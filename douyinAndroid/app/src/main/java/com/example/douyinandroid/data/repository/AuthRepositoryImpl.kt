package com.example.douyinandroid.data.repository

import com.example.douyinandroid.core.core_auth.AuthPreferences
import com.example.douyinandroid.core.core_network.network.ApiService
import com.example.douyinandroid.core.core_network.network.bean.LoginRequest
import com.example.douyinandroid.core.core_network.network.bean.RefreshTokenRequest
import com.example.douyinandroid.core.core_network.network.bean.RegisterRequest
import com.example.douyinandroid.domain.model.Result
import com.example.douyinandroid.domain.repository.AuthRepository
import com.example.douyinandroid.domain.repository.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoginResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(
                    LoginRequest(
                        loginType = LoginRequest.LOGIN_TYPE_PASSWORD,
                        username = username,
                        password = password
                    )
                )
                if (response.isSuccess && response.data != null) {
                    val loginData = response.data
                    authPreferences.saveLoginData(
                        userId = loginData.userId,
                        username = loginData.username,
                        nickname = loginData.nickname,
                        avatar = loginData.avatar,
                        token = loginData.token
                    )
                    Result.Success(
                        LoginResult(
                            userId = loginData.userId,
                            username = loginData.username,
                            nickname = loginData.nickname,
                            avatar = loginData.avatar,
                            token = loginData.token,
                            refreshToken = null,
                            expiresIn = 7 * 24 * 60 * 60L
                        )
                    )
                } else {
                    Result.Error(
                        Exception(response.message),
                        response.message
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun loginByPhone(phone: String, code: String): Result<LoginResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(
                    LoginRequest(
                        loginType = LoginRequest.LOGIN_TYPE_PHONE,
                        phone = phone,
                        password = code
                    )
                )
                if (response.isSuccess && response.data != null) {
                    val loginData = response.data
                    authPreferences.saveLoginData(
                        userId = loginData.userId,
                        username = loginData.username,
                        nickname = loginData.nickname,
                        avatar = loginData.avatar,
                        token = loginData.token
                    )
                    Result.Success(
                        LoginResult(
                            userId = loginData.userId,
                            username = loginData.username,
                            nickname = loginData.nickname,
                            avatar = loginData.avatar,
                            token = loginData.token,
                            refreshToken = null,
                            expiresIn = 7 * 24 * 60 * 60L
                        )
                    )
                } else {
                    Result.Error(
                        Exception(response.message),
                        response.message
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        nickname: String,
        phone: String?,
        email: String?
    ): Result<LoginResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(
                    RegisterRequest(
                        username = username,
                        password = password,
                        nickname = nickname,
                        phone = phone,
                        email = email
                    )
                )
                if (response.isSuccess && response.data != null) {
                    val loginData = response.data
                    authPreferences.saveLoginData(
                        userId = loginData.userId,
                        username = loginData.username,
                        nickname = loginData.nickname,
                        avatar = loginData.avatar,
                        token = loginData.token
                    )
                    Result.Success(
                        LoginResult(
                            userId = loginData.userId,
                            username = loginData.username,
                            nickname = loginData.nickname,
                            avatar = loginData.avatar,
                            token = loginData.token,
                            refreshToken = null,
                            expiresIn = 7 * 24 * 60 * 60L
                        )
                    )
                } else {
                    Result.Error(
                        Exception(response.message),
                        response.message
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.logout()
                authPreferences.clearLoginData()
                Result.Success(Unit)
            } catch (e: Exception) {
                authPreferences.clearLoginData()
                Result.Success(Unit)
            }
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<LoginResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                if (response.isSuccess && response.data != null) {
                    val tokenData = response.data
                    authPreferences.token = tokenData.token
                    tokenData.refreshToken?.let { authPreferences.refreshToken = it }
                    Result.Success(
                        LoginResult(
                            userId = authPreferences.userId,
                            username = authPreferences.username,
                            nickname = authPreferences.nickname ?: "",
                            avatar = authPreferences.avatar,
                            token = tokenData.token,
                            refreshToken = tokenData.refreshToken,
                            expiresIn = 7 * 24 * 60 * 60L
                        )
                    )
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override fun isLoggedIn(): Boolean {
        return authPreferences.isLoggedIn
    }

    override fun getCurrentUserId(): Long? {
        return if (authPreferences.isLoggedIn) authPreferences.userId else null
    }

    override fun getToken(): String? {
        return authPreferences.token
    }
}
