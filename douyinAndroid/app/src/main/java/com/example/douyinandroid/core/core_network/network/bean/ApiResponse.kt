package com.example.douyinandroid.core.core_network.network.bean

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T?,
    @SerializedName("requestId")
    val requestId: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null
) {
    val isSuccess: Boolean
        get() = code == 200
}

data class PageResponse<T>(
    @SerializedName("items")
    val items: List<T>?,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("hasMore")
    val hasMore: Boolean
)
