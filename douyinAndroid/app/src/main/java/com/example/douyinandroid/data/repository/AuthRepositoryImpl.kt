package com.example.douyinandroid.data.repository

import com.example.douyinandroid.common.common_utils.LogUtil
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

private const val TAG = "AuthRepository"

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoginResult> {
        LogUtil.d(TAG, "login started: username=$username")
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
                    LogUtil.d(TAG, "login API success: userId=${loginData.userId}, username=${loginData.username}, nickname=${loginData.nickname}")
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
                    LogUtil.w(TAG, "login API failed: code=${response.code}, message=${response.message}")
                    Result.Error(
                        Exception(response.message),
                        response.message
                    )
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "login exception: username=$username", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun loginByPhone(phone: String, code: String): Result<LoginResult> {
        LogUtil.d(TAG, "loginByPhone started: phone=${phone.maskPhone()}, codeLength=${code.length}")
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
                    LogUtil.d(TAG, "loginByPhone API success: userId=${loginData.userId}, username=${loginData.username}")
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
                    LogUtil.w(TAG, "loginByPhone API failed: code=${response.code}, message=${response.message}")
                    Result.Error(
                        Exception(response.message),
                        response.message
                    )
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "loginByPhone exception: phone=${phone.maskPhone()}", e)
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
        LogUtil.d(
            TAG,
            "register started: username=$username, nickname=$nickname, phonePresent=${!phone.isNullOrBlank()}, emailPresent=${!email.isNullOrBlank()}"
        )
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
                    LogUtil.d(TAG, "register API success: userId=${loginData.userId}, username=${loginData.username}, nickname=${loginData.nickname}")
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
                    LogUtil.w(TAG, "register API failed: code=${response.code}, message=${response.message}")
                    Result.Error(
                        Exception(response.message),
                        response.message
                    )
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "register exception: username=$username", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        LogUtil.d(TAG, "logout started: currentUserId=${authPreferences.userId}")
        return withContext(Dispatchers.IO) {
            try {
                apiService.logout()
                authPreferences.clearLoginData()
                LogUtil.d(TAG, "logout API success, local auth data cleared")
                Result.Success(Unit)
            } catch (e: Exception) {
                authPreferences.clearLoginData()
                LogUtil.w(TAG, "logout API failed, local auth data cleared anyway: ${e.message}")
                Result.Success(Unit)
            }
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<LoginResult> {
        LogUtil.d(TAG, "refreshToken started: refreshTokenLength=${refreshToken.length}")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                if (response.isSuccess && response.data != null) {
                    val tokenData = response.data
                    authPreferences.token = tokenData.token
                    tokenData.refreshToken?.let { authPreferences.refreshToken = it }
                    LogUtil.d(TAG, "refreshToken API success: userId=${authPreferences.userId}, newTokenLength=${tokenData.token.length}")
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
                    LogUtil.w(TAG, "refreshToken API failed: code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "refreshToken exception", e)
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

    private fun String.maskPhone(): String {
        return if (length >= 7) {
            take(3) + "****" + takeLast(4)
        } else {
            "***"
        }
    }
}
