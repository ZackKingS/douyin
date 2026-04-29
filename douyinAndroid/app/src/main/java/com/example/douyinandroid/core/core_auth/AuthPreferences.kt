package com.example.douyinandroid.core.core_auth

import android.content.Context
import android.content.SharedPreferences

class AuthPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var userId: Long
        get() = prefs.getLong(KEY_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_USER_ID, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var nickname: String?
        get() = prefs.getString(KEY_NICKNAME, null)
        set(value) = prefs.edit().putString(KEY_NICKNAME, value).apply()

    var avatar: String?
        get() = prefs.getString(KEY_AVATAR, null)
        set(value) = prefs.edit().putString(KEY_AVATAR, value).apply()

    var tokenExpiresAt: Long
        get() = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_TOKEN_EXPIRES_AT, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrEmpty() && userId != -1L

    fun saveLoginData(
        userId: Long,
        username: String?,
        nickname: String,
        avatar: String?,
        token: String,
        refreshToken: String? = null,
        expiresIn: Long = DEFAULT_EXPIRES_IN
    ) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            if (username != null) putString(KEY_USERNAME, username) else remove(KEY_USERNAME)
            putString(KEY_NICKNAME, nickname)
            if (avatar != null) putString(KEY_AVATAR, avatar) else remove(KEY_AVATAR)
            putString(KEY_TOKEN, token)
            if (refreshToken != null) putString(KEY_REFRESH_TOKEN, refreshToken) else remove(KEY_REFRESH_TOKEN)
            putLong(KEY_TOKEN_EXPIRES_AT, System.currentTimeMillis() + expiresIn * 1000)
            apply()
        }
    }

    fun clearLoginData() {
        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_NICKNAME)
            remove(KEY_AVATAR)
            remove(KEY_TOKEN_EXPIRES_AT)
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "douyin_auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val DEFAULT_EXPIRES_IN = 7 * 24 * 60 * 60L // 7 days

        @Volatile
        private var INSTANCE: AuthPreferences? = null

        fun getInstance(context: Context): AuthPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
