package com.example.douyinandroid.core.core_network.network.bean

import com.google.gson.annotations.SerializedName

// ==================== 视频相关 ====================

data class VideoListPageResponse(
    @SerializedName("items")
    val items: List<VideoItem>?,
    @SerializedName("nextTime")
    val nextTime: Long?,
    @SerializedName("hasMore")
    val hasMore: Boolean
) {
    val list: List<VideoItem>? get() = items
}

data class VideoDetailResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("videoId")
    val videoId: String,
    @SerializedName("author")
    val author: VideoAuthor?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("videoUrl")
    val videoUrl: String,
    @SerializedName("coverUrl")
    val coverUrl: String,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("likeCount")
    val likeCount: Long,
    @SerializedName("commentCount")
    val commentCount: Long,
    @SerializedName("shareCount")
    val shareCount: Long,
    @SerializedName("collectCount")
    val collectCount: Long,
    @SerializedName("viewCount")
    val viewCount: Long,
    @SerializedName("isLiked")
    val isLiked: Boolean,
    @SerializedName("isCollected")
    val isCollected: Boolean,
    @SerializedName("location")
    val location: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("topicIds")
    val topicIds: List<String>?,
    @SerializedName("musicId")
    val musicId: Long?,
    @SerializedName("createTime")
    val createTime: String?
)

data class VideoItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("videoId")
    val videoId: String,
    @SerializedName("author")
    val author: VideoAuthor?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("videoUrl")
    val videoUrl: String,
    @SerializedName("coverUrl")
    val coverUrl: String,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("likeCount")
    val likeCount: Long,
    @SerializedName("commentCount")
    val commentCount: Long,
    @SerializedName("shareCount")
    val shareCount: Long,
    @SerializedName("collectCount")
    val collectCount: Long,
    @SerializedName("viewCount")
    val viewCount: Long,
    @SerializedName("isLiked")
    val isLiked: Boolean,
    @SerializedName("isCollected")
    val isCollected: Boolean,
    @SerializedName("topicName")
    val topicName: String?,
    @SerializedName("music")
    val music: VideoMusic?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("topicIds")
    val topicIds: List<String>?,
    @SerializedName("createTime")
    val createTime: String?
)

data class VideoMusic(
    @SerializedName("musicId")
    val musicId: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("author")
    val author: String?,
    @SerializedName("albumCover")
    val albumCover: String?
)

data class VideoAuthor(
    @SerializedName("userId")
    val id: Long,
    @SerializedName("username")
    val username: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("signature")
    val signature: String? = null,
    @SerializedName("followCount")
    val followCount: Long = 0,
    @SerializedName("fansCount")
    val fansCount: Long = 0,
    @SerializedName("isFollowing")
    val isFollowing: Boolean = false,
    @SerializedName("isMutual")
    val isMutual: Boolean = false,
    @SerializedName("level")
    val level: Int = 0
)

data class VideoActionResponse(
    @SerializedName("videoId")
    val videoId: String,
    @SerializedName("likeCount")
    val likeCount: Long? = null,
    @SerializedName("collectCount")
    val collectCount: Long? = null
)

// ==================== 评论相关 ====================

data class CommentCreateRequest(
    @SerializedName("content")
    val content: String,
    @SerializedName("parentId")
    val parentId: String? = null,
    @SerializedName("atUserIds")
    val atUserIds: String? = null
)

data class CommentListPageResponse(
    @SerializedName("items")
    val items: List<CommentItem>?,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("totalPages")
    val totalPages: Int? = null,
    @SerializedName("list")
    val legacyList: List<CommentItem>? = null,
    @SerializedName("hasMore")
    val hasMore: Boolean = false
) {
    val list: List<CommentItem>? get() = items ?: legacyList
}

data class CommentItem(
    @SerializedName("commentId")
    val commentId: String,
    @SerializedName("videoId")
    val videoId: Long? = null,
    @SerializedName("userId")
    val userId: Long? = null,
    @SerializedName("user")
    val user: VideoAuthor?,
    @SerializedName("content")
    val content: String,
    @SerializedName("likeCount")
    val likeCount: Long,
    @SerializedName("replyCount")
    val replyCount: Long,
    @SerializedName("parentId")
    val parentId: Long? = null,
    @SerializedName("rootId")
    val rootId: Long? = null,
    @SerializedName("isLiked")
    val isLiked: Boolean,
    @SerializedName("createTime")
    val createTime: String?,
    @SerializedName("replies")
    val replies: List<CommentItem>?
)

data class CommentResponse(
    @SerializedName("commentId")
    val commentId: String,
    @SerializedName("videoId")
    val videoId: Long? = null,
    @SerializedName("userId")
    val userId: Long? = null,
    @SerializedName("user")
    val user: VideoAuthor? = null,
    @SerializedName("content")
    val content: String,
    @SerializedName("likeCount")
    val likeCount: Long,
    @SerializedName("replyCount")
    val replyCount: Long,
    @SerializedName("parentId")
    val parentId: Long? = null,
    @SerializedName("rootId")
    val rootId: Long? = null,
    @SerializedName("createTime")
    val createTime: String?
)

data class CommentActionResponse(
    @SerializedName("commentId")
    val commentId: Long,
    @SerializedName("likeCount")
    val likeCount: Long
)

// ==================== 用户信息 (简版，用于视频/评论中的用户) ====================

data class UserInfo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("username")
    val username: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("signature")
    val signature: String? = null,
    @SerializedName("followCount")
    val followCount: Long = 0,
    @SerializedName("fansCount")
    val fansCount: Long = 0,
    @SerializedName("isFollowing")
    val isFollowing: Boolean = false
)
