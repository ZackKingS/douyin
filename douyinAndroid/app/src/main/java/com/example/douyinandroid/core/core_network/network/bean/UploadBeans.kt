package com.example.douyinandroid.core.core_network.network.bean

import com.google.gson.annotations.SerializedName

// ==================== 上传相关 ====================

data class VideoUploadResponse(
    @SerializedName("videoId")
    val videoId: String,
    @SerializedName("videoUrl")
    val videoUrl: String,
    @SerializedName("coverUrl")
    val coverUrl: String,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)

data class FileUploadResponse(
    @SerializedName("filename")
    val filename: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("mimeType")
    val mimeType: String
)
