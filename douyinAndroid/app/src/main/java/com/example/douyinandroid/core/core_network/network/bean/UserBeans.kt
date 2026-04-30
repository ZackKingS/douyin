package com.example.douyinandroid.core.core_network.network.bean

import com.google.gson.annotations.SerializedName

// ==================== 用户相关 ====================

data class UserInfoResponse(
    @SerializedName(value = "userId", alternate = ["id"])
    val id: Long,
    @SerializedName("username")
    val username: String?,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("gender")
    val gender: Int = 0,
    @SerializedName("birthday")
    val birthday: String?,
    @SerializedName("signature")
    val signature: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("province")
    val province: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("followCount")
    val followCount: Long = 0,
    @SerializedName("fansCount")
    val fansCount: Long = 0,
    @SerializedName("likeCount")
    val likeCount: Long = 0,
    @SerializedName("videoCount")
    val videoCount: Long = 0,
    @SerializedName("isFollowing")
    val isFollowing: Boolean = false
)

data class UserUpdateRequest(
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("gender")
    val gender: Int? = null,
    @SerializedName("birthday")
    val birthday: String? = null,
    @SerializedName("signature")
    val signature: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("province")
    val province: String? = null,
    @SerializedName("city")
    val city: String? = null
)

data class UserListPageResponse(
    @SerializedName("items")
    val items: List<UserInfo>? = null,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("totalPages")
    val totalPages: Int? = null,
    @SerializedName("list")
    val legacyList: List<UserInfo>? = null,
    @SerializedName("hasMore")
    val hasMore: Boolean = false
) {
    val list: List<UserInfo>? get() = items ?: legacyList
}

data class FollowActionResponse(
    @SerializedName("followId")
    val followId: Long? = null,
    @SerializedName("isFollowing")
    val isFollowing: Boolean? = null,
    @SerializedName("followCount")
    val followCount: Long? = null,
    @SerializedName("fansCount")
    val fansCount: Long? = null
)

// ==================== 认证相关 ====================

data class LoginRequest(
    @SerializedName("loginType")
    val loginType: String,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("phone")
    val phone: String? = null
) {
    companion object {
        const val LOGIN_TYPE_PASSWORD = "password"
        const val LOGIN_TYPE_PHONE = "phone"
    }
}

data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("email")
    val email: String? = null
)

data class LoginResponse(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("username")
    val username: String?,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("token")
    val token: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    @SerializedName("expiresIn")
    val expiresIn: Long = 7 * 24 * 60 * 60L
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class TokenResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("refreshToken")
    val refreshToken: String?
)
