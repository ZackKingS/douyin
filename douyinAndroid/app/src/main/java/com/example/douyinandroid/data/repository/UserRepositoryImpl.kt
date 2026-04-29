package com.example.douyinandroid.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.douyinandroid.core.core_database.database.dao.UserDao
import com.example.douyinandroid.core.core_network.network.ApiService
import com.example.douyinandroid.core.core_network.network.bean.LoginRequest
import com.example.douyinandroid.core.core_network.network.bean.RegisterRequest
import com.example.douyinandroid.domain.model.*
import com.example.douyinandroid.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUserProfile(userId: Long): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserInfo(userId)
                if (response.isSuccess && response.data != null) {
                    val user = response.data.toDomain()
                    userDao.insertUser(response.data.toEntity())
                    Result.Success(user)
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun getUserFollows(userId: Long, page: Int, size: Int): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserFollowing(userId, page, size)
                if (response.isSuccess && response.data != null) {
                    val users = response.data.list?.map { it.toDomain() } ?: emptyList()
                    Result.Success(users)
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun getUserFans(userId: Long, page: Int, size: Int): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserFollowers(userId, page, size)
                if (response.isSuccess && response.data != null) {
                    val users = response.data.list?.map { it.toDomain() } ?: emptyList()
                    Result.Success(users)
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun followUser(userId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.followUser(userId)
                if (response.isSuccess) {
                    userDao.updateFollowStatus(userId, true)
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun unfollowUser(userId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.unfollowUser(userId)
                if (response.isSuccess) {
                    userDao.updateFollowStatus(userId, false)
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override suspend fun getUserVideos(userId: Long, page: Int, size: Int): Result<List<Video>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserVideos(userId, page, size)
                if (response.isSuccess && response.data != null) {
                    val videos = response.data.list?.map { it.toDomain() } ?: emptyList()
                    Result.Success(videos)
                } else {
                    Result.Error(Exception(response.message), response.message)
                }
            } catch (e: Exception) {
                Result.Error(e, e.message)
            }
        }
    }

    override fun getLocalUser(userId: Long): LiveData<User?> {
        return userDao.getUserByIdLive(userId).map { entity ->
            entity?.toDomain()
        }
    }
}

// Extension functions
private fun com.example.douyinandroid.core.core_network.network.bean.UserInfoResponse.toDomain(): User {
    return User(
        userId = id,
        nickname = nickname,
        avatar = avatar,
        signature = signature,
        fansCount = fansCount,
        followCount = followCount,
        isFollowing = isFollowing
    )
}

private fun com.example.douyinandroid.core.core_network.network.bean.UserInfo.toDomain(): User {
    return User(
        userId = id,
        nickname = nickname ?: "",
        avatar = avatar,
        fansCount = fansCount,
        followCount = followCount,
        isFollowing = isFollowing
    )
}

private fun com.example.douyinandroid.core.core_network.network.bean.UserInfoResponse.toEntity(): com.example.douyinandroid.core.core_database.database.entity.UserEntity {
    return com.example.douyinandroid.core.core_database.database.entity.UserEntity(
        id = id,
        username = username,
        nickname = nickname,
        avatar = avatar,
        gender = gender,
        birthday = birthday,
        signature = signature,
        country = country,
        province = province,
        city = city,
        followCount = followCount,
        fansCount = fansCount,
        likeCount = likeCount,
        videoCount = videoCount,
        isFollowing = isFollowing,
        isFollowed = isFollowing,
        isMutual = false,
        level = 0,
        badge = null,
        updateTime = System.currentTimeMillis()
    )
}

private fun com.example.douyinandroid.core.core_database.database.entity.UserEntity.toDomain(): User {
    return User(
        userId = id,
        nickname = nickname,
        avatar = avatar,
        signature = signature,
        fansCount = fansCount,
        followCount = followCount,
        isFollowing = isFollowing
    )
}

private fun com.example.douyinandroid.core.core_network.network.bean.VideoItem.toDomain(): Video {
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
