package com.example.douyinandroid.domain.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.douyinandroid.domain.model.*

interface VideoRepository {
    suspend fun getVideoFeed(page: Int = 1, size: Int = 10): Result<List<Video>>
    suspend fun getVideoDetail(videoId: String): Result<Video>
    suspend fun likeVideo(videoId: String): Result<Unit>
    suspend fun unlikeVideo(videoId: String): Result<Unit>
    suspend fun shareVideo(videoId: String, platform: String): Result<String>
    suspend fun getVideoComments(videoId: String, page: Int = 1, size: Int = 20): Result<List<Comment>>
    suspend fun postComment(videoId: String, content: String, parentId: String? = null): Result<Comment>

    fun getLocalVideos(): LiveData<List<Video>>
    fun getVideoById(videoId: String): LiveData<Video?>
    suspend fun getVideosByAuthor(authorId: Long): List<Video>
}

interface UserRepository {
    suspend fun getUserProfile(userId: Long): Result<User>
    suspend fun getUserFollows(userId: Long, page: Int = 1, size: Int = 20): Result<List<User>>
    suspend fun getUserFans(userId: Long, page: Int = 1, size: Int = 20): Result<List<User>>
    suspend fun followUser(userId: Long): Result<Unit>
    suspend fun unfollowUser(userId: Long): Result<Unit>
    suspend fun getUserVideos(userId: Long, page: Int = 1, size: Int = 20): Result<List<Video>>

    fun getLocalUser(userId: Long): LiveData<User?>
}

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResult>
    suspend fun loginByPhone(phone: String, code: String): Result<LoginResult>
    suspend fun register(
        username: String,
        password: String,
        nickname: String,
        phone: String? = null,
        email: String? = null
    ): Result<LoginResult>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<LoginResult>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): Long?
    fun getToken(): String?
}

data class LoginResult(
    val userId: Long,
    val username: String?,
    val nickname: String,
    val avatar: String?,
    val token: String,
    val refreshToken: String?,
    val expiresIn: Long
)

interface SearchRepository {
    suspend fun search(keyword: String, type: String = "all", page: Int = 1, size: Int = 20): Result<SearchResult>
    suspend fun getSearchHot(): Result<List<HotSearchItem>>
}

data class SearchResult(
    val keyword: String,
    val users: List<User>,
    val videos: List<Video>,
    val topics: List<Topic>
)

data class HotSearchItem(
    val rank: Int,
    val word: String,
    val hotValue: Long,
    val hotTrend: String?
)

interface PublishRepository {
    suspend fun uploadVideo(
        videoUri: Uri,
        title: String,
        description: String?,
        topicIds: List<Long>?,
        location: String?,
        coverUri: Uri?,
        onProgress: (Int) -> Unit
    ): Result<PublishResult>

    suspend fun uploadCover(
        coverUri: Uri,
        onProgress: (Int) -> Unit
    ): Result<String>
}

data class PublishResult(
    val videoId: String,
    val title: String?,
    val status: String,
    val coverUrl: String?,
    val videoUrl: String?
)
