package com.example.douyinandroid.core.core_database.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val title: String?,
    val description: String?,
    val videoUrl: String,
    val coverUrl: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val authorId: Long,
    val authorNickname: String?,
    val authorAvatar: String?,
    val likeCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val collectCount: Long,
    val viewCount: Long,
    val isLiked: Boolean,
    val isCollected: Boolean,
    val topicName: String?,
    val musicTitle: String?,
    val musicAuthor: String?,
    val location: String?,
    val createTime: Long
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val username: String?,
    val nickname: String,
    val avatar: String?,
    val gender: Int,
    val birthday: String?,
    val signature: String?,
    val country: String?,
    val province: String?,
    val city: String?,
    val followCount: Long,
    val fansCount: Long,
    val likeCount: Long,
    val videoCount: Long,
    val isFollowing: Boolean,
    val isFollowed: Boolean,
    val isMutual: Boolean,
    val level: Int,
    val badge: String?,
    val updateTime: Long
)

@Entity(tableName = "cache")
data class CacheEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val expireTime: Long
)

@Entity(tableName = "follows")
data class FollowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val followId: Long,
    val createTime: Long
)

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val userId: Long,
    val createTime: Long
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val userId: Long,
    val watchTime: Long,
    val progress: Float,
    val createTime: Long
)
