package com.example.douyinandroid.data.repository

import android.content.Context
import android.net.Uri
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_network.network.ApiService
import com.example.douyinandroid.core.core_network.network.ContentUriRequestBody
import com.example.douyinandroid.domain.model.Result
import com.example.douyinandroid.domain.repository.PublishRepository
import com.example.douyinandroid.domain.repository.PublishResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "PublishRepository"

class PublishRepositoryImpl(
    private val context: Context,
    private val apiService: ApiService
) : PublishRepository {

    override suspend fun uploadVideo(
        videoUri: Uri,
        title: String,
        description: String?,
        topicIds: List<Long>?,
        location: String?,
        coverUri: Uri?,
        onProgress: (Int) -> Unit
    ): Result<PublishResult> {
        LogUtil.d(
            TAG,
            "uploadVideo started: videoUri=$videoUri, titleLength=${title.length}, descriptionPresent=${!description.isNullOrBlank()}, topicCount=${topicIds?.size ?: 0}, locationPresent=${!location.isNullOrBlank()}, coverPresent=${coverUri != null}"
        )
        return withContext(Dispatchers.IO) {
            try {
                // Get media type from URI
                val mimeType = context.contentResolver.getType(videoUri)
                    ?: getMimeTypeFromExtension(videoUri)
                val mediaType = mimeType?.toMediaTypeOrNull()
                    ?: "video/mp4".toMediaTypeOrNull()
                LogUtil.d(TAG, "Video mime resolved: mimeType=$mimeType, mediaType=$mediaType")

                // Create video request body with progress tracking
                val videoRequestBody = ContentUriRequestBody(context, videoUri, mediaType, onProgress)

                val videoFileName = videoRequestBody.getFileName()
                val videoPart = MultipartBody.Part.createFormData("file", videoFileName, videoRequestBody)
                LogUtil.d(TAG, "Video part created: fileName=$videoFileName, contentLength=${videoRequestBody.contentLength()}")

                // Create text parts
                val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
                val topicIdsBody = topicIds?.joinToString(",")?.toRequestBody("text/plain".toMediaTypeOrNull())
                val locationBody = location?.toRequestBody("text/plain".toMediaTypeOrNull())

                // Create cover part if provided
                val coverPart = coverUri?.let { uri ->
                    val coverMimeType = context.contentResolver.getType(uri)
                        ?: getMimeTypeFromExtension(uri)
                    val coverMediaType = coverMimeType?.toMediaTypeOrNull()
                        ?: "image/jpeg".toMediaTypeOrNull()

                    val coverRequestBody = ContentUriRequestBody(context, uri, coverMediaType, null)
                    val coverFileName = coverRequestBody.getFileName()
                    LogUtil.d(TAG, "Cover part created: fileName=$coverFileName, mimeType=$coverMimeType, contentLength=${coverRequestBody.contentLength()}")
                    MultipartBody.Part.createFormData("cover", coverFileName, coverRequestBody)
                }

                LogUtil.d(TAG, "Calling uploadVideo API")
                val response = apiService.uploadVideo(
                    videoFile = videoPart,
                    title = titleBody,
                    description = descriptionBody,
                    topicIds = topicIdsBody,
                    location = locationBody,
                    coverFile = coverPart,
                    latitude = null,
                    longitude = null,
                    atUserIds = null,
                    musicId = null
                )

                if (response.isSuccess && response.data != null) {
                    onProgress(100)
                    val data = response.data
                    LogUtil.d(TAG, "uploadVideo API success: videoId=${data.videoId}, videoUrl=${data.videoUrl}, coverUrl=${data.coverUrl}")
                    Result.Success(
                        PublishResult(
                            videoId = data.videoId,
                            title = title,
                            status = "success",
                            coverUrl = data.coverUrl,
                            videoUrl = data.videoUrl
                        )
                    )
                } else {
                    LogUtil.w(TAG, "uploadVideo API failed: code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "uploadVideo exception: videoUri=$videoUri", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun uploadCover(
        coverUri: Uri,
        onProgress: (Int) -> Unit
    ): Result<String> {
        LogUtil.d(TAG, "uploadCover started: coverUri=$coverUri")
        return withContext(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(coverUri)
                    ?: getMimeTypeFromExtension(coverUri)
                val mediaType = mimeType?.toMediaTypeOrNull()
                    ?: "image/jpeg".toMediaTypeOrNull()
                LogUtil.d(TAG, "Cover mime resolved: mimeType=$mimeType, mediaType=$mediaType")

                val coverRequestBody = ContentUriRequestBody(context, coverUri, mediaType, onProgress)

                val coverFileName = coverRequestBody.getFileName()
                val part = MultipartBody.Part.createFormData("file", coverFileName, coverRequestBody)
                val typeBody = "cover".toRequestBody("text/plain".toMediaTypeOrNull())
                LogUtil.d(TAG, "Calling uploadFile API for cover: fileName=$coverFileName, contentLength=${coverRequestBody.contentLength()}")

                val response = apiService.uploadFile(part, typeBody)

                if (response.isSuccess && response.data != null) {
                    onProgress(100)
                    LogUtil.d(TAG, "uploadCover API success: url=${response.data.url}")
                    Result.Success(response.data.url)
                } else {
                    LogUtil.w(TAG, "uploadCover API failed: code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "uploadCover exception: coverUri=$coverUri", e)
                Result.Error(e, e.message)
            }
        }
    }

    private fun getMimeTypeFromExtension(uri: Uri): String? {
        val extension = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
        return when (extension) {
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "m4v" -> "video/x-m4v"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> {
                LogUtil.w(TAG, "Unknown mime type extension for uri=$uri")
                null
            }
        }
    }
}
