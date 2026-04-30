package com.example.douyinandroid.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_database.database.dao.VideoDao
import com.example.douyinandroid.core.core_network.network.ApiService
import com.example.douyinandroid.core.core_network.network.bean.VideoItem
import com.example.douyinandroid.core.core_network.network.bean.CommentItem
import com.example.douyinandroid.core.core_network.network.bean.CommentCreateRequest
import com.example.douyinandroid.domain.model.*
import com.example.douyinandroid.domain.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "VideoRepository"

class VideoRepositoryImpl(
    private val apiService: ApiService,
    private val videoDao: VideoDao
) : VideoRepository {

    override suspend fun getVideoFeed(page: Int, size: Int): Result<List<Video>> {
        LogUtil.d(TAG, "getVideoFeed called: page=$page, size=$size")
        return withContext(Dispatchers.IO) {
            try {
                LogUtil.d(TAG, "Calling API getVideoList...")
                val response = apiService.getVideoList("recommend", page, size)
                LogUtil.d(TAG, "API response received: isSuccess=${response.isSuccess}, code=${response.code}, message=${response.message}")
                LogUtil.d(TAG, "API response data: ${response.data}")
                LogUtil.d(TAG, "API response data.list: ${response.data?.list}")
                LogUtil.d(TAG, "API response data.list?.size: ${response.data?.list?.size}")

                if (response.isSuccess && response.data != null) {
                    val videos = response.data.list?.map { it.toDomain() } ?: emptyList()
                    LogUtil.d(TAG, "Converted ${videos.size} videos from API response")
                    videos.forEachIndexed { index, video ->
                        LogUtil.d(TAG, "Video[$index]: id=${video.id}, title=${video.title}, videoUrl=${video.videoUrl}, coverUrl=${video.coverUrl}")
                    }
                    Result.Success(videos)
                } else {
                    LogUtil.e(TAG, "API response failed: ${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "Exception in getVideoFeed", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun getVideoDetail(videoId: String): Result<Video> {
        LogUtil.d(TAG, "getVideoDetail called: videoId=$videoId")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVideoDetail(videoId)
                if (response.isSuccess && response.data != null) {
                    val video = response.data.toDomain()
                    LogUtil.d(TAG, "getVideoDetail success: videoId=${video.id}, title=${video.title}")
                    Result.Success(video)
                } else {
                    LogUtil.w(TAG, "getVideoDetail failed: videoId=$videoId, code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "getVideoDetail exception: videoId=$videoId", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun likeVideo(videoId: String): Result<Unit> {
        LogUtil.d(TAG, "likeVideo called: videoId=$videoId")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.likeVideo(videoId)
                if (response.isSuccess) {
                    videoDao.updateLikeStatus(videoId, true, 1)
                    LogUtil.d(TAG, "likeVideo success and local cache updated: videoId=$videoId")
                    Result.Success(Unit)
                } else {
                    LogUtil.w(TAG, "likeVideo failed: videoId=$videoId, code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "likeVideo exception: videoId=$videoId", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun unlikeVideo(videoId: String): Result<Unit> {
        LogUtil.d(TAG, "unlikeVideo called: videoId=$videoId")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.unlikeVideo(videoId)
                if (response.isSuccess) {
                    videoDao.updateLikeStatus(videoId, false, -1)
                    LogUtil.d(TAG, "unlikeVideo success and local cache updated: videoId=$videoId")
                    Result.Success(Unit)
                } else {
                    LogUtil.w(TAG, "unlikeVideo failed: videoId=$videoId, code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "unlikeVideo exception: videoId=$videoId", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun shareVideo(videoId: String, platform: String): Result<String> {
        LogUtil.d(TAG, "shareVideo called: videoId=$videoId, platform=$platform")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.shareVideo(videoId, platform)
                if (response.isSuccess && response.data != null) {
                    LogUtil.d(TAG, "shareVideo success: videoId=$videoId, platform=$platform")
                    Result.Success(response.data.shareUrl ?: "")
                } else {
                    LogUtil.w(TAG, "shareVideo failed: videoId=$videoId, platform=$platform, code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "shareVideo exception: videoId=$videoId, platform=$platform", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun deleteVideo(videoId: String): Result<Unit> {
        LogUtil.d(TAG, "deleteVideo called: videoId=$videoId")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteVideo(videoId)
                if (response.isSuccess) {
                    videoDao.deleteVideoById(videoId)
                    LogUtil.d(TAG, "deleteVideo success and local cache updated: videoId=$videoId")
                    Result.Success(Unit)
                } else {
                    LogUtil.w(TAG, "deleteVideo failed: videoId=$videoId, code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "deleteVideo exception: videoId=$videoId", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun getVideoComments(videoId: String, page: Int, size: Int): Result<List<Comment>> {
        LogUtil.d(TAG, "getVideoComments called: videoId=$videoId, page=$page, size=$size")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVideoComments(videoId, page, size)
                if (response.isSuccess && response.data != null) {
                    val comments = response.data.list?.map { it.toDomain() } ?: emptyList()
                    LogUtil.d(TAG, "getVideoComments success: videoId=$videoId, count=${comments.size}")
                    Result.Success(comments)
                } else {
                    LogUtil.w(TAG, "getVideoComments failed: videoId=$videoId, code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "getVideoComments exception: videoId=$videoId", e)
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun postComment(videoId: String, content: String, parentId: String?): Result<Comment> {
        LogUtil.d(TAG, "postComment called: videoId=$videoId, contentLength=${content.length}, parentId=$parentId")
        return withContext(Dispatchers.IO) {
            try {
                val request = CommentCreateRequest(
                    content = content,
                    parentId = parentId
                )
                val response = apiService.createComment(videoId, request)
                if (response.isSuccess && response.data != null) {
                    LogUtil.d(TAG, "postComment success: videoId=$videoId, commentId=${response.data.commentId}")
                    Result.Success(response.data.toDomain())
                } else {
                    LogUtil.w(TAG, "postComment failed: videoId=$videoId, code=${response.code}, message=${response.message}")
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "postComment exception: videoId=$videoId", e)
                Result.Error(e, e.message)
            }
        }
    }

    override fun getLocalVideos(): LiveData<List<Video>> {
        return videoDao.getAllVideos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVideoById(videoId: String): LiveData<Video?> {
        return videoDao.getVideoByIdLive(videoId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getVideosByAuthor(authorId: Long): List<Video> {
        LogUtil.d(TAG, "getVideosByAuthor called: authorId=$authorId")
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserVideos(authorId)
                if (response.isSuccess && response.data != null) {
                    val videos = response.data.list?.map { it.toDomain() } ?: emptyList()
                    LogUtil.d(TAG, "getVideosByAuthor success: authorId=$authorId, count=${videos.size}")
                    videos
                } else {
                    LogUtil.w(TAG, "getVideosByAuthor failed: authorId=$authorId, code=${response.code}, message=${response.message}")
                    emptyList()
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "getVideosByAuthor exception: authorId=$authorId", e)
                emptyList()
            }
        }
    }
}

// Extension functions for mapping
private fun VideoItem.toDomain(): Video {
    return Video(
        id = videoId,
        title = title,
        description = description,
        videoUrl = videoUrl,
        coverUrl = coverUrl,
        duration = duration,
        width = width,
        height = height,
        author = author?.toDomain(),
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        collectCount = collectCount,
        viewCount = viewCount,
        isLiked = isLiked,
        isCollected = isCollected,
        topicName = topicName,
        music = music?.let {
            com.example.douyinandroid.domain.model.Music(
                musicId = it.musicId ?: "",
                title = it.title,
                author = it.author,
                albumCover = it.albumCover
            )
        },
        location = location,
        createTime = createTime
    )
}

private fun com.example.douyinandroid.core.core_network.network.bean.VideoDetailResponse.toDomain(): Video {
    return Video(
        id = videoId,
        title = title,
        description = description,
        videoUrl = videoUrl,
        coverUrl = coverUrl,
        duration = duration,
        width = width,
        height = height,
        author = author?.let {
            User(
                userId = it.id,
                nickname = it.nickname ?: "",
                avatar = it.avatar,
                signature = it.signature,
                fansCount = it.fansCount,
                followCount = it.followCount,
                isFollowing = it.isFollowing
            )
        },
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        collectCount = collectCount,
        viewCount = viewCount,
        isLiked = isLiked,
        isCollected = isCollected,
        topicName = null,
        music = null,
        location = location,
        createTime = createTime
    )
}

private fun com.example.douyinandroid.core.core_network.network.bean.VideoAuthor.toDomain(): User {
    return User(
        userId = id,
        nickname = nickname ?: "",
        avatar = avatar,
        signature = signature,
        fansCount = fansCount,
        followCount = followCount,
        isFollowing = isFollowing
    )
}

private fun CommentItem.toDomain(): Comment {
    return Comment(
        commentId = commentId,
        user = user?.toDomain(),
        content = content,
        likeCount = likeCount,
        replyCount = replyCount,
        isLiked = isLiked,
        createTime = createTime,
        replies = replies?.map { it.toDomain() }
    )
}

private fun com.example.douyinandroid.core.core_network.network.bean.CommentResponse.toDomain(): Comment {
    return Comment(
        commentId = commentId,
        user = user?.toDomain(),
        content = content,
        likeCount = likeCount,
        replyCount = replyCount,
        isLiked = false,
        createTime = createTime,
        replies = null
    )
}

private fun com.example.douyinandroid.core.core_database.database.entity.VideoEntity.toDomain(): Video {
    return Video(
        id = id,
        title = title,
        description = description,
        videoUrl = videoUrl,
        coverUrl = coverUrl,
        duration = duration,
        width = width,
        height = height,
        author = User(
            userId = authorId,
            nickname = authorNickname ?: "",
            avatar = authorAvatar
        ),
        likeCount = likeCount,
        commentCount = commentCount,
        shareCount = shareCount,
        collectCount = collectCount,
        viewCount = viewCount,
        isLiked = isLiked,
        isCollected = isCollected,
        topicName = topicName,
        music = if (musicTitle != null) {
            Music(musicId = "", title = musicTitle, author = musicAuthor, albumCover = null)
        } else null,
        location = location,
        createTime = null
    )
}
