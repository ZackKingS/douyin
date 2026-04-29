package com.example.douyinandroid.data.repository

import android.content.Context
import android.net.Uri
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
        return withContext(Dispatchers.IO) {
            try {
                // Get media type from URI
                val mimeType = context.contentResolver.getType(videoUri)
                    ?: getMimeTypeFromExtension(videoUri)
                val mediaType = mimeType?.toMediaTypeOrNull()
                    ?: "video/mp4".toMediaTypeOrNull()

                // Create video request body with progress tracking
                val videoRequestBody = ContentUriRequestBody(context, videoUri, mediaType, onProgress)

                val videoFileName = videoRequestBody.getFileName()
                val videoPart = MultipartBody.Part.createFormData("file", videoFileName, videoRequestBody)

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
                    MultipartBody.Part.createFormData("cover", coverFileName, coverRequestBody)
                }

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
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun uploadCover(
        coverUri: Uri,
        onProgress: (Int) -> Unit
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(coverUri)
                    ?: getMimeTypeFromExtension(coverUri)
                val mediaType = mimeType?.toMediaTypeOrNull()
                    ?: "image/jpeg".toMediaTypeOrNull()

                val coverRequestBody = ContentUriRequestBody(context, coverUri, mediaType, onProgress)

                val coverFileName = coverRequestBody.getFileName()
                val part = MultipartBody.Part.createFormData("file", coverFileName, coverRequestBody)
                val typeBody = "cover".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadFile(part, typeBody)

                if (response.isSuccess && response.data != null) {
                    onProgress(100)
                    Result.Success(response.data.url)
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
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
            else -> null
        }
    }
}
